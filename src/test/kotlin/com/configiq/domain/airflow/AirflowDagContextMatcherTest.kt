package com.configiq.domain.airflow

import com.configiq.inspection.AirflowScheduleInspection
import com.configiq.preview.PreviewAirflowScheduleIntention
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralValue
import com.intellij.psi.impl.FakePsiElement
import org.junit.Test
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AirflowDagContextMatcherTest {
    @Test
    fun findsScheduleKeywordViaStablePsiApis() {
        val target =
            createTarget(
                keyword = "schedule",
                scheduleText = "0 12 * * *",
                callText =
                    """
                    with DAG(
                        dag_id="stable_schedule_keyword",
                        schedule="0 12 * * *",
                    ) as dag:
                        pass
                    """.trimIndent(),
            )

        assertNotNull(target)
        assertEquals("schedule", target.keyword)
        assertEquals("0 12 * * *", target.scheduleText)
    }

    @Test
    fun findsScheduleIntervalKeywordViaStablePsiApis() {
        val target =
            createTarget(
                keyword = "schedule_interval",
                scheduleText = "@daily",
                callText =
                    """
                    dag = DAG(
                        dag_id="stable_schedule_interval_keyword",
                        schedule_interval="@daily",
                    )
                    """.trimIndent(),
            )

        assertNotNull(target)
        assertEquals("schedule_interval", target.keyword)
        assertEquals("@daily", target.scheduleText)
    }

    @Test
    fun ignoresMatchingKeywordOutsideDagCall() {
        val target =
            createTarget(
                keyword = "schedule",
                scheduleText = "0 12 * * *",
                callText =
                    """
                    build_job(
                        schedule="0 12 * * *",
                    )
                    """.trimIndent(),
            )

        assertNull(target)
    }

    @Test
    fun contextMatcherBytecodeDoesNotReferencePythonPluginApis() {
        val classText = readClassText(AirflowDagContextMatcher::class.java)

        assertFalse(classText.contains("com/jetbrains/python/"))
        assertTrue(classText.contains("com/intellij/psi/PsiNamedElement"))
        assertTrue(classText.contains("com/intellij/psi/PsiLiteralValue"))
    }

    @Test
    fun inspectionBytecodeDoesNotReferencePythonPluginApis() {
        val classText = readClassText(AirflowScheduleInspection::class.java)

        assertFalse(classText.contains("com/jetbrains/python/"))
    }

    @Test
    fun previewIntentionBytecodeDoesNotReferencePythonPluginApis() {
        val classText = readClassText(PreviewAirflowScheduleIntention::class.java)

        assertFalse(classText.contains("com/jetbrains/python/"))
    }

    private fun createTarget(
        keyword: String,
        scheduleText: String,
        callText: String,
    ): AirflowScheduleTarget? {
        val call = TestPsiElement(elementText = callText)
        val keywordArgument = TestPsiElement(elementText = """$keyword="$scheduleText"""", elementName = keyword)
        val literal = TestLiteralElement(text = "\"$scheduleText\"", literalValue = scheduleText)
        val caret = TestPsiElement(elementText = scheduleText)

        link(call, keywordArgument)
        link(keywordArgument, literal)
        link(literal, caret)

        return AirflowDagContextMatcher.findScheduleTarget(caret)
    }

    private fun link(
        parent: TestPsiElement,
        child: TestPsiElement,
    ) {
        parent.firstChildElement = child
        child.parentElement = parent
    }

    private fun readClassText(clazz: Class<*>): String {
        val classBytes =
            clazz
                .getResourceAsStream("${clazz.simpleName}.class")
                ?.readBytes()
                ?: throw AssertionError("Failed to read ${clazz.simpleName}.class bytes")
        return String(classBytes, StandardCharsets.ISO_8859_1)
    }

    private open class TestPsiElement(
        private val elementText: String,
        private val elementName: String? = null,
    ) : FakePsiElement() {
        var parentElement: PsiElement? = null
        var firstChildElement: PsiElement? = null
        var nextSiblingElement: PsiElement? = null

        override fun getParent(): PsiElement? = parentElement

        override fun getFirstChild(): PsiElement? = firstChildElement

        override fun getNextSibling(): PsiElement? = nextSiblingElement

        override fun getText(): String = elementText

        override fun getName(): String? = elementName
    }

    private class TestLiteralElement(
        text: String,
        private val literalValue: String,
    ) : TestPsiElement(text),
        PsiLiteralValue {
        override fun getValue(): Any = literalValue
    }
}
