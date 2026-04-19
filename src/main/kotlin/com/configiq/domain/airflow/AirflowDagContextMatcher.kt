package com.configiq.domain.airflow

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralValue
import com.intellij.psi.PsiNamedElement

private val AIRFLOW_SCHEDULE_KEYWORDS = setOf("schedule", "schedule_interval")
private val DAG_CALL_PATTERN = Regex("""(?:^|[^\w.])(?:[\w.]+\.)?DAG\s*\(""")

data class AirflowScheduleTarget(
    val keyword: String,
    val literal: PsiElement,
    val scheduleText: String,
)

object AirflowDagContextMatcher {
    fun findScheduleTarget(element: PsiElement?): AirflowScheduleTarget? {
        val literal = generateSequence(element) { it.parent }
            .firstOrNull { extractStringValue(it) != null }
            ?: return null
        return fromLiteral(literal)
    }

    fun fromLiteral(literal: PsiElement): AirflowScheduleTarget? {
        if (extractStringValue(literal) == null) {
            return null
        }

        val keywordArgument = generateSequence(literal.parent) { it.parent }
            .firstOrNull { extractKeyword(it) in AIRFLOW_SCHEDULE_KEYWORDS }
            ?: return null
        return fromKeywordArgument(keywordArgument)
    }

    fun fromKeywordArgument(keywordArgument: PsiElement): AirflowScheduleTarget? {
        val keyword = extractKeyword(keywordArgument) ?: return null
        if (keyword !in AIRFLOW_SCHEDULE_KEYWORDS) {
            return null
        }

        val literal = findStringLiteral(keywordArgument) ?: return null
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

    private fun extractKeyword(element: PsiElement): String? =
        (element as? PsiNamedElement)?.name

    private fun extractStringValue(element: PsiElement): String? =
        (element as? PsiLiteralValue)?.value as? String

    private fun findStringLiteral(root: PsiElement): PsiElement? {
        extractStringValue(root)?.let { return root }

        var child = root.firstChild
        while (child != null) {
            val literal = findStringLiteral(child)
            if (literal != null) {
                return literal
            }
            child = child.nextSibling
        }

        return null
    }

    private fun isDagCall(keywordArgument: PsiElement): Boolean =
        generateSequence(keywordArgument.parent) { it.parent }
            .any { DAG_CALL_PATTERN.containsMatchIn(it.text) }
}
