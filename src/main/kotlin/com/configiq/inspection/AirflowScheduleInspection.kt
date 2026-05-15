package com.configiq.inspection

import com.configiq.domain.airflow.AirflowDagContextMatcher
import com.configiq.domain.airflow.AirflowScheduleParser
import com.configiq.domain.airflow.AirflowScheduleValidationResult
import com.configiq.settings.ConfigIqSettingsService
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class AirflowScheduleInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor {
        if (!ConfigIqSettingsService.getInstance().isAirflowPackEnabled()) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                val target = AirflowDagContextMatcher.fromKeywordArgument(element) ?: return
                when (val validation = AirflowScheduleParser.validate(target.scheduleText)) {
                    is AirflowScheduleValidationResult.Valid -> Unit
                    is AirflowScheduleValidationResult.Invalid -> {
                        holder.registerProblem(target.literal, validation.message)
                    }
                }
            }
        }
    }
}
