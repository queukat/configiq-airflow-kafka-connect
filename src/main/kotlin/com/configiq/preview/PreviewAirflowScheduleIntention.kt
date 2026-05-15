package com.configiq.preview

import com.configiq.domain.airflow.AirflowDagContextMatcher
import com.configiq.domain.airflow.AirflowScheduleParser
import com.configiq.domain.airflow.AirflowScheduleValidationResult
import com.configiq.settings.ConfigIqSettingsService
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PreviewAirflowScheduleIntention : PsiElementBaseIntentionAction() {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")

    override fun getFamilyName(): String = "Show Airflow schedule result"

    override fun getText(): String = "Show Airflow schedule result"

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (!ConfigIqSettingsService.getInstance().isAirflowPackEnabled()) {
            return false
        }

        val target = AirflowDagContextMatcher.findScheduleTarget(element) ?: return false
        return AirflowScheduleParser.validate(target.scheduleText) is AirflowScheduleValidationResult.Valid
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        val editor = editor ?: return
        val target = AirflowDagContextMatcher.findScheduleTarget(element) ?: return
        val validation = AirflowScheduleParser.validate(target.scheduleText) as? AirflowScheduleValidationResult.Valid ?: return

        val nextRuns = validation.schedule.previewNextRuns(ZonedDateTime.now(), 5)
        val message =
            buildString {
                append("<html><body>")
                append("<b>Airflow schedule result</b><br/>")
                append(StringUtil.escapeXmlEntities(validation.schedule.previewLabel))
                append("<br/>")
                append(StringUtil.escapeXmlEntities(validation.schedule.resultSummary))

                if (nextRuns.isEmpty()) {
                    append("<br/><br/>No recurring preview is available.")
                } else {
                    append("<br/><br/><b>Next Airflow runs</b><br/>")
                    nextRuns.forEachIndexed { index, runTime ->
                        append(index + 1)
                        append(". ")
                        append(runTime.format(formatter))
                        append("<br/>")
                    }
                }
                append("</body></html>")
            }

        HintManager.getInstance().showInformationHint(editor, message)
    }

    override fun startInWriteAction(): Boolean = false
}
