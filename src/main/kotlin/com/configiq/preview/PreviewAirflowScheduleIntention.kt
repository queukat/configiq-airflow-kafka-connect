package com.configiq.preview

import com.configiq.domain.airflow.AirflowDagContextMatcher
import com.configiq.domain.airflow.AirflowScheduleParser
import com.configiq.domain.airflow.AirflowScheduleValidationResult
import com.configiq.settings.ConfigIqSettingsService
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PreviewAirflowScheduleIntention : PsiElementBaseIntentionAction() {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")

    override fun getFamilyName(): String = "Preview Airflow schedule"

    override fun getText(): String = "Preview next Airflow runs"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (!ConfigIqSettingsService.getInstance().isAirflowPackEnabled()) {
            return false
        }

        val target = AirflowDagContextMatcher.findScheduleTarget(element) ?: return false
        val validation = AirflowScheduleParser.validate(target.scheduleText) as? AirflowScheduleValidationResult.Valid ?: return false
        return validation.schedule.supportsPreview()
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val editor = editor ?: return
        val target = AirflowDagContextMatcher.findScheduleTarget(element) ?: return
        val validation = AirflowScheduleParser.validate(target.scheduleText) as? AirflowScheduleValidationResult.Valid ?: return

        val nextRuns = validation.schedule.previewNextRuns(ZonedDateTime.now(), 5)
        val message = if (nextRuns.isEmpty()) {
            "No preview is available for ${validation.schedule.previewLabel}."
        } else {
            buildString {
                append("<html><body>")
                append("<b>Next Airflow runs</b><br/>")
                append("${validation.schedule.previewLabel}<br/><br/>")
                nextRuns.forEachIndexed { index, runTime ->
                    append(index + 1)
                    append(". ")
                    append(runTime.format(formatter))
                    append("<br/>")
                }
                append("</body></html>")
            }
        }

        HintManager.getInstance().showInformationHint(editor, message)
    }

    override fun startInWriteAction(): Boolean = false
}
