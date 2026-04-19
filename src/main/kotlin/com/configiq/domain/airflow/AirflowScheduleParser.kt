package com.configiq.domain.airflow

import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.max

sealed interface AirflowScheduleValidationResult {
    data class Valid(val schedule: ParsedAirflowSchedule) : AirflowScheduleValidationResult
    data class Invalid(val message: String) : AirflowScheduleValidationResult
}

data class ParsedAirflowSchedule(
    val normalizedText: String,
    val previewLabel: String,
    val resultSummary: String,
    private val nextRunsProvider: ((ZonedDateTime, Int) -> List<ZonedDateTime>)?,
) {
    fun supportsPreview(): Boolean = nextRunsProvider != null

    fun previewNextRuns(from: ZonedDateTime, count: Int): List<ZonedDateTime> {
        val provider = nextRunsProvider ?: return emptyList()
        return provider(from, max(count, 0))
    }
}

object AirflowScheduleParser {
    private const val MAX_SEARCH_MINUTES = 525_600

    private val monthAliases = mapOf(
        "jan" to 1,
        "feb" to 2,
        "mar" to 3,
        "apr" to 4,
        "may" to 5,
        "jun" to 6,
        "jul" to 7,
        "aug" to 8,
        "sep" to 9,
        "oct" to 10,
        "nov" to 11,
        "dec" to 12,
    )

    private val dayAliases = mapOf(
        "sun" to 0,
        "mon" to 1,
        "tue" to 2,
        "wed" to 3,
        "thu" to 4,
        "fri" to 5,
        "sat" to 6,
    )

    private val macroCronMappings = mapOf(
        "@hourly" to "0 * * * *",
        "@daily" to "0 0 * * *",
        "@midnight" to "0 0 * * *",
        "@weekly" to "0 0 * * 0",
        "@monthly" to "0 0 1 * *",
        "@yearly" to "0 0 1 1 *",
        "@annually" to "0 0 1 1 *",
        "@once" to null,
    )

    fun validate(scheduleText: String): AirflowScheduleValidationResult {
        val normalized = scheduleText.trim()
        if (normalized.isEmpty()) {
            return AirflowScheduleValidationResult.Invalid("Airflow schedule is empty.")
        }

        val lowered = normalized.lowercase()
        if (macroCronMappings.containsKey(lowered)) {
            return AirflowScheduleValidationResult.Valid(createMacroSchedule(lowered))
        }

        val parts = normalized.split(Regex("\\s+"))
        if (parts.size != 5) {
            return AirflowScheduleValidationResult.Invalid("Cron schedule must have exactly 5 fields, got ${parts.size}.")
        }

        val normalizedCron = parts.joinToString(" ")

        val minuteField = parseField(parts[0], 0..59, "minute") ?: return invalidField("minute", parts[0])
        val hourField = parseField(parts[1], 0..23, "hour") ?: return invalidField("hour", parts[1])
        val dayOfMonthField = parseField(parts[2], 1..31, "day of month") ?: return invalidField("day of month", parts[2])
        val monthField = parseField(parts[3], 1..12, "month", monthAliases) ?: return invalidField("month", parts[3])
        val dayOfWeekField = parseField(parts[4], 0..7, "day of week", dayAliases) ?: return invalidField("day of week", parts[4])

        val cronSpec = CronSpec(
            minute = minuteField,
            hour = hourField,
            dayOfMonth = dayOfMonthField,
            month = monthField,
            dayOfWeek = dayOfWeekField,
        )

        return AirflowScheduleValidationResult.Valid(
            ParsedAirflowSchedule(
                normalizedText = normalizedCron,
                previewLabel = normalizedCron,
                resultSummary = "Normalized cron: $normalizedCron",
                nextRunsProvider = cronSpec::nextRuns,
            ),
        )
    }

    private fun invalidField(fieldName: String, rawValue: String): AirflowScheduleValidationResult.Invalid {
        return AirflowScheduleValidationResult.Invalid("Cron $fieldName field '$rawValue' is not supported.")
    }

    private fun createMacroSchedule(macro: String): ParsedAirflowSchedule {
        val cronExpression = macroCronMappings[macro]
        if (cronExpression == null) {
            return ParsedAirflowSchedule(
                normalizedText = macro,
                previewLabel = macro,
                resultSummary = "Runs once, so there is no recurring cron preview.",
                nextRunsProvider = null,
            )
        }

        val validation = validate(cronExpression) as AirflowScheduleValidationResult.Valid
        return ParsedAirflowSchedule(
            normalizedText = macro,
            previewLabel = macro,
            resultSummary = "Resolved to cron: $cronExpression",
            nextRunsProvider = validation.schedule::previewNextRuns,
        )
    }

    private fun parseField(
        rawField: String,
        allowedRange: IntRange,
        fieldName: String,
        aliases: Map<String, Int> = emptyMap(),
    ): CronField? {
        val wildcard = rawField == "*"
        val values = sortedSetOf<Int>()

        for (segment in rawField.split(',')) {
            if (!addSegment(values, segment.trim(), allowedRange, aliases)) {
                return null
            }
        }

        if (values.isEmpty()) {
            return null
        }

        return CronField(
            values = values,
            wildcard = wildcard,
        )
    }

    private fun addSegment(
        values: MutableSet<Int>,
        rawSegment: String,
        allowedRange: IntRange,
        aliases: Map<String, Int>,
    ): Boolean {
        if (rawSegment.isEmpty()) {
            return false
        }

        val stepSplit = rawSegment.split('/', limit = 2)
        val base = stepSplit[0]
        val step = stepSplit.getOrNull(1)?.toIntOrNull()
        if (step != null && step <= 0) {
            return false
        }
        if (stepSplit.size == 2 && step == null) {
            return false
        }

        val rangeValues = when {
            base == "*" -> allowedRange.toList()
            '-' in base -> {
                val bounds = base.split('-', limit = 2)
                val start = parseToken(bounds[0], aliases) ?: return false
                val end = parseToken(bounds[1], aliases) ?: return false
                if (start > end) {
                    return false
                }
                (start..end).toList()
            }
            else -> {
                val singleValue = parseToken(base, aliases) ?: return false
                listOf(singleValue)
            }
        }

        val steppedValues = if (step != null && rangeValues.size > 1) {
            rangeValues.filterIndexed { index, _ -> index % step == 0 }
        } else if (step != null && base == "*") {
            rangeValues.filterIndexed { index, _ -> index % step == 0 }
        } else if (step != null && rangeValues.size == 1) {
            generateSequence(rangeValues.first()) { current ->
                val next = current + step
                if (next <= allowedRange.last) next else null
            }.toList()
        } else {
            rangeValues
        }

        if (steppedValues.isEmpty()) {
            return false
        }

        if (steppedValues.any { it !in allowedRange }) {
            return false
        }

        values += steppedValues.map { normalizeDayOfWeek(it) }
        return true
    }

    private fun parseToken(token: String, aliases: Map<String, Int>): Int? {
        val normalized = token.trim().lowercase()
        return aliases[normalized] ?: normalized.toIntOrNull()
    }

    private fun normalizeDayOfWeek(value: Int): Int {
        return if (value == 7) 0 else value
    }

    private data class CronField(
        val values: Set<Int>,
        val wildcard: Boolean,
    ) {
        fun matches(value: Int): Boolean = wildcard || value in values
    }

    private data class CronSpec(
        val minute: CronField,
        val hour: CronField,
        val dayOfMonth: CronField,
        val month: CronField,
        val dayOfWeek: CronField,
    ) {
        fun nextRuns(from: ZonedDateTime, count: Int): List<ZonedDateTime> {
            if (count <= 0) {
                return emptyList()
            }

            val matches = mutableListOf<ZonedDateTime>()
            var cursor = from.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1)

            repeat(MAX_SEARCH_MINUTES) {
                if (matches(cursor)) {
                    matches += cursor
                    if (matches.size == count) {
                        return matches
                    }
                }
                cursor = cursor.plusMinutes(1)
            }

            return matches
        }

        private fun matches(dateTime: ZonedDateTime): Boolean {
            if (!minute.matches(dateTime.minute)) return false
            if (!hour.matches(dateTime.hour)) return false
            if (!month.matches(dateTime.monthValue)) return false

            val dayOfMonthMatches = dayOfMonth.matches(dateTime.dayOfMonth)
            val cronDayOfWeek = dateTime.dayOfWeek.toCronValue()
            val dayOfWeekMatches = dayOfWeek.matches(cronDayOfWeek)

            val dayMatches = when {
                dayOfMonth.wildcard && dayOfWeek.wildcard -> true
                dayOfMonth.wildcard -> dayOfWeekMatches
                dayOfWeek.wildcard -> dayOfMonthMatches
                else -> dayOfMonthMatches || dayOfWeekMatches
            }

            return dayMatches
        }
    }

    private fun DayOfWeek.toCronValue(): Int = value % 7
}
