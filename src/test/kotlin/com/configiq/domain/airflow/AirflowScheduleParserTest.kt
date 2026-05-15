package com.configiq.domain.airflow

import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AirflowScheduleParserTest {
    @Test
    fun validatesFiveFieldCronAndBuildsPreview() {
        val validation = AirflowScheduleParser.validate("0 12 * * *") as AirflowScheduleValidationResult.Valid

        assertEquals("0 12 * * *", validation.schedule.normalizedText)
        assertEquals("Normalized cron: 0 12 * * *", validation.schedule.resultSummary)

        val runs =
            validation.schedule.previewNextRuns(
                ZonedDateTime.of(2026, 3, 31, 11, 45, 0, 0, ZoneId.of("UTC")),
                3,
            )

        assertEquals(3, runs.size)
        assertEquals("2026-03-31T12:00Z[UTC]", runs[0].toString())
        assertEquals("2026-04-01T12:00Z[UTC]", runs[1].toString())
        assertEquals("2026-04-02T12:00Z[UTC]", runs[2].toString())
    }

    @Test
    fun acceptsDailyMacroPreview() {
        val validation = AirflowScheduleParser.validate("@daily") as AirflowScheduleValidationResult.Valid

        assertTrue(validation.schedule.supportsPreview())
        assertEquals("Resolved to cron: 0 0 * * *", validation.schedule.resultSummary)
        assertEquals(2, validation.schedule.previewNextRuns(ZonedDateTime.of(2026, 3, 31, 23, 0, 0, 0, ZoneId.of("UTC")), 2).size)
    }

    @Test
    fun normalizesWhitespaceInsideCronPreviewSummary() {
        val validation = AirflowScheduleParser.validate("  0   6   * *   1-5  ") as AirflowScheduleValidationResult.Valid

        assertEquals("0 6 * * 1-5", validation.schedule.normalizedText)
        assertEquals("Normalized cron: 0 6 * * 1-5", validation.schedule.resultSummary)
    }

    @Test
    fun previewsSteppedCronFields() {
        val validation = AirflowScheduleParser.validate("*/15 * * * *") as AirflowScheduleValidationResult.Valid

        val runs =
            validation.schedule.previewNextRuns(
                ZonedDateTime.of(2026, 3, 31, 12, 1, 0, 0, ZoneId.of("UTC")),
                3,
            )

        assertEquals("2026-03-31T12:15Z[UTC]", runs[0].toString())
        assertEquals("2026-03-31T12:30Z[UTC]", runs[1].toString())
        assertEquals("2026-03-31T12:45Z[UTC]", runs[2].toString())
    }

    @Test
    fun previewsSingleValueSteps() {
        val validation = AirflowScheduleParser.validate("5/20 * * * *") as AirflowScheduleValidationResult.Valid

        val runs =
            validation.schedule.previewNextRuns(
                ZonedDateTime.of(2026, 3, 31, 12, 0, 0, 0, ZoneId.of("UTC")),
                3,
            )

        assertEquals("2026-03-31T12:05Z[UTC]", runs[0].toString())
        assertEquals("2026-03-31T12:25Z[UTC]", runs[1].toString())
        assertEquals("2026-03-31T12:45Z[UTC]", runs[2].toString())
    }

    @Test
    fun supportsMonthAndDayAliases() {
        val validation = AirflowScheduleParser.validate("0 9 * jan mon-fri") as AirflowScheduleValidationResult.Valid

        val runs =
            validation.schedule.previewNextRuns(
                ZonedDateTime.of(2026, 1, 1, 8, 0, 0, 0, ZoneId.of("UTC")),
                3,
            )

        assertEquals("2026-01-01T09:00Z[UTC]", runs[0].toString())
        assertEquals("2026-01-02T09:00Z[UTC]", runs[1].toString())
        assertEquals("2026-01-05T09:00Z[UTC]", runs[2].toString())
    }

    @Test
    fun appliesCronDayOfMonthAndDayOfWeekSemantics() {
        val dayOfMonthOnly = AirflowScheduleParser.validate("0 9 15 * *") as AirflowScheduleValidationResult.Valid
        val dayOfWeekOnly = AirflowScheduleParser.validate("0 9 * * mon") as AirflowScheduleValidationResult.Valid
        val dayOfMonthOrWeek = AirflowScheduleParser.validate("0 9 15 * mon") as AirflowScheduleValidationResult.Valid
        val from = ZonedDateTime.of(2026, 6, 1, 9, 1, 0, 0, ZoneId.of("UTC"))

        assertEquals("2026-06-15T09:00Z[UTC]", firstPreview(dayOfMonthOnly, from))
        assertEquals("2026-06-08T09:00Z[UTC]", firstPreview(dayOfWeekOnly, from))
        assertEquals("2026-06-08T09:00Z[UTC]", firstPreview(dayOfMonthOrWeek, from))
    }

    @Test
    fun rejectsUnsupportedSegments() {
        assertTrue(AirflowScheduleParser.validate("*/0 * * * *") is AirflowScheduleValidationResult.Invalid)
        assertTrue(AirflowScheduleParser.validate("*/bogus * * * *") is AirflowScheduleValidationResult.Invalid)
        assertTrue(AirflowScheduleParser.validate("10-5 * * * *") is AirflowScheduleValidationResult.Invalid)
        assertTrue(AirflowScheduleParser.validate("0,,5 * * * *") is AirflowScheduleValidationResult.Invalid)
        assertTrue(AirflowScheduleParser.validate("0 9 * nope *") is AirflowScheduleValidationResult.Invalid)
    }

    @Test
    fun explainsOnceMacroWithoutPreview() {
        val validation = AirflowScheduleParser.validate("@once") as AirflowScheduleValidationResult.Valid

        assertEquals("@once", validation.schedule.normalizedText)
        assertEquals("Runs once, so there is no recurring cron preview.", validation.schedule.resultSummary)
        assertTrue(!validation.schedule.supportsPreview())
    }

    @Test
    fun rejectsWrongFieldCount() {
        val validation = AirflowScheduleParser.validate("0 12 * *") as AirflowScheduleValidationResult.Invalid

        assertEquals("Cron schedule must have exactly 5 fields, got 4.", validation.message)
    }

    private fun firstPreview(
        validation: AirflowScheduleValidationResult.Valid,
        from: ZonedDateTime,
    ): String =
        validation.schedule
            .previewNextRuns(from, 1)
            .single()
            .toString()
}
