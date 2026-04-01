package com.configiq.domain.airflow

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralValue
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyKeywordArgument
import com.jetbrains.python.psi.PyStringLiteralExpression

private val AIRFLOW_SCHEDULE_KEYWORDS = setOf("schedule", "schedule_interval")

data class AirflowScheduleTarget(
    val keyword: String,
    val literal: PyStringLiteralExpression,
    val scheduleText: String,
)

object AirflowDagContextMatcher {
    fun findScheduleTarget(element: PsiElement?): AirflowScheduleTarget? {
        val literal = PsiTreeUtil.getParentOfType(element, PyStringLiteralExpression::class.java, false) ?: return null
        return fromLiteral(literal)
    }

    fun fromLiteral(literal: PyStringLiteralExpression): AirflowScheduleTarget? {
        val keywordArgument = literal.parent as? PyKeywordArgument ?: return null
        return fromKeywordArgument(keywordArgument)
    }

    fun fromKeywordArgument(keywordArgument: PyKeywordArgument): AirflowScheduleTarget? {
        val keyword = extractKeyword(keywordArgument) ?: return null
        if (keyword !in AIRFLOW_SCHEDULE_KEYWORDS) {
            return null
        }

        val literal = keywordArgument.valueExpression as? PyStringLiteralExpression ?: return null
        val scheduleText = extractStringValue(literal) ?: return null
        if (!isDagCall(keywordArgument)) {
            return null
        }

        return AirflowScheduleTarget(
            keyword = keyword,
            literal = literal,
            scheduleText = scheduleText,
        )
    }

    private fun extractKeyword(keywordArgument: PyKeywordArgument): String? =
        (keywordArgument as PsiNamedElement).name

    private fun extractStringValue(literal: PyStringLiteralExpression): String? =
        (literal as? PsiLiteralValue)?.value as? String

    private fun isDagCall(keywordArgument: PyKeywordArgument): Boolean {
        val callExpression = PsiTreeUtil.getParentOfType(keywordArgument, PyCallExpression::class.java, false) ?: return false
        val calleeName = callExpression.callee?.text?.substringAfterLast('.')
        return calleeName == "DAG"
    }
}
