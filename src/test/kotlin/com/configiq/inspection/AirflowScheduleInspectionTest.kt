package com.configiq.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.python.PythonFileType

class AirflowScheduleInspectionTest : BasePlatformTestCase() {
    fun testHighlightsInvalidScheduleInsideDagCall() {
        myFixture.enableInspections(AirflowScheduleInspection())
        myFixture.configureByText(
            PythonFileType.INSTANCE,
            """
            from airflow import DAG
            
            with DAG(
                dag_id="invalid_schedule_demo",
                schedule=<warning descr="Cron schedule must have exactly 5 fields, got 4.">"0 12 * *"</warning>,
            ) as dag:
                pass
            """.trimIndent(),
        )

        myFixture.checkHighlighting()
    }

    fun testIgnoresMatchingKeywordOutsideDagCall() {
        myFixture.enableInspections(AirflowScheduleInspection())
        myFixture.configureByText(
            PythonFileType.INSTANCE,
            """
            schedule = "0 12 * *"
            
            def build_job(schedule):
                return schedule
            """.trimIndent(),
        )

        myFixture.checkHighlighting()
    }
}
