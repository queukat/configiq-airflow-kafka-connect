package com.configiq.preview

import com.configiq.settings.ConfigIqSettingsService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralValue
import com.intellij.psi.impl.FakePsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PreviewAirflowScheduleIntentionTest : BasePlatformTestCase() {
    override fun tearDown() {
        try {
            ConfigIqSettingsService.getInstance().loadState(ConfigIqSettingsService.State())
        } finally {
            super.tearDown()
        }
    }

    fun testMetadataUsesStableText() {
        val intention = PreviewAirflowScheduleIntention()

        assertEquals("Show Airflow schedule result", intention.familyName)
        assertEquals("Show Airflow schedule result", intention.text)
        assertFalse(intention.startInWriteAction())
    }

    fun testAvailabilityHonorsDisabledAirflowPack() {
        ConfigIqSettingsService.getInstance().setAirflowPackEnabled(false)

        val intention = PreviewAirflowScheduleIntention()

        assertFalse(intention.isAvailable(project, null, createScheduleLiteral()))
    }

    fun testAvailabilityAcceptsValidDagSchedule() {
        ConfigIqSettingsService.getInstance().setAirflowPackEnabled(true)

        val intention = PreviewAirflowScheduleIntention()

        assertTrue(intention.isAvailable(project, null, createScheduleLiteral()))
    }

    fun testInvokeReturnsWhenEditorIsMissing() {
        PreviewAirflowScheduleIntention().invoke(project, null, createScheduleLiteral())
    }

    private fun createScheduleLiteral(): PsiElement {
        val call =
            TestPsiElement(
                elementText =
                    """
                    with DAG(
                        dag_id="orders",
                        schedule="0 12 * * *",
                    ) as dag:
                        pass
                    """.trimIndent(),
            )
        val keywordArgument = TestPsiElement(elementText = """schedule="0 12 * * *"""", elementName = "schedule")
        val literal = TestLiteralElement(text = "\"0 12 * * *\"", literalValue = "0 12 * * *")

        link(call, keywordArgument)
        link(keywordArgument, literal)

        return literal
    }

    private fun link(
        parent: TestPsiElement,
        child: TestPsiElement,
    ) {
        parent.firstChildElement = child
        child.parentElement = parent
    }

    private open class TestPsiElement(
        private val elementText: String,
        private val elementName: String? = null,
    ) : FakePsiElement() {
        var parentElement: PsiElement? = null
        var firstChildElement: PsiElement? = null

        override fun getParent(): PsiElement? = parentElement

        override fun getFirstChild(): PsiElement? = firstChildElement

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
