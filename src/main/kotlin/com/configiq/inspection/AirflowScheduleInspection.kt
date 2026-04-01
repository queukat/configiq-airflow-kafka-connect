package com.configiq.inspection

import com.configiq.domain.airflow.AirflowDagContextMatcher
import com.configiq.domain.airflow.AirflowScheduleParser
import com.configiq.domain.airflow.AirflowScheduleValidationResult
import com.configiq.settings.ConfigIqSettingsService
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.PyElementVisitor
import com.jetbrains.python.psi.PyKeywordArgument

class AirflowScheduleInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!ConfigIqSettingsService.getInstance().isAirflowPackEnabled()) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        return object : PyElementVisitor() {
            override fun visitPyKeywordArgument(node: PyKeywordArgument) {
                val target = AirflowDagContextMatcher.fromKeywordArgument(node) ?: return
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
