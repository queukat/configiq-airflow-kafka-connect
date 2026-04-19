package com.configiq.domain.airflow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.time.ZoneId
import java.time.ZonedDateTime

class AirflowScheduleParserTest {
    @Test
    fun validatesFiveFieldCronAndBuildsPreview() {
        val validation = AirflowScheduleParser.validate("0 12 * * *") as AirflowScheduleValidationResult.Valid

        assertEquals("0 12 * * *", validation.schedule.normalizedText)
        assertEquals("Normalized cron: 0 12 * * *", validation.schedule.resultSummary)

        val runs = validation.schedule.previewNextRuns(
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
}
