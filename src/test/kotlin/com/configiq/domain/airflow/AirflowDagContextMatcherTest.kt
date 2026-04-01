package com.configiq.domain.airflow

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.python.PythonFileType
import java.nio.charset.StandardCharsets

class AirflowDagContextMatcherTest : BasePlatformTestCase() {
    fun testFindsScheduleKeywordViaStablePsiApis() {
        val target = configureTarget(
            """
            from airflow import DAG

            with DAG(
                dag_id="stable_schedule_keyword",
                schedule="<caret>0 12 * * *",
            ) as dag:
                pass
            """.trimIndent(),
        )

        assertNotNull(target)
        assertEquals("schedule", target!!.keyword)
        assertEquals("0 12 * * *", target.scheduleText)
    }

    fun testFindsScheduleIntervalKeywordViaStablePsiApis() {
        val target = configureTarget(
            """
            from airflow import DAG

            dag = DAG(
                dag_id="stable_schedule_interval_keyword",
                schedule_interval="<caret>@daily",
            )
            """.trimIndent(),
        )

        assertNotNull(target)
        assertEquals("schedule_interval", target!!.keyword)
        assertEquals("@daily", target.scheduleText)
    }

    fun testBytecodeDoesNotReferenceExperimentalPythonAstApis() {
        val classBytes = AirflowDagContextMatcher::class.java
            .getResourceAsStream("AirflowDagContextMatcher.class")
            ?.readBytes()
            ?: throw AssertionError("Failed to read AirflowDagContextMatcher.class bytes")
        val classText = String(classBytes, StandardCharsets.ISO_8859_1)

        assertFalse(classText.contains("com/jetbrains/python/ast/PyAstKeywordArgument"))
        assertFalse(classText.contains("com/jetbrains/python/ast/PyAstStringLiteralExpression"))
        assertTrue(classText.contains("com/intellij/psi/PsiNamedElement"))
        assertTrue(classText.contains("com/intellij/psi/PsiLiteralValue"))
    }

    private fun configureTarget(code: String): AirflowScheduleTarget? {
        myFixture.configureByText(PythonFileType.INSTANCE, code)
        val elementAtCaret = myFixture.file.findElementAt(myFixture.caretOffset)
        return AirflowDagContextMatcher.findScheduleTarget(elementAtCaret)
    }
}
