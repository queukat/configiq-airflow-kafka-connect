package com.configiq.quickfix

import com.configiq.domain.kconnect.KConnectConfigFormat
import com.configiq.domain.kconnect.KConnectConfigModel
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager

class AddKConnectEntryQuickFix(
    private val key: String,
    private val value: String,
) : LocalQuickFix {
    override fun getFamilyName(): String = "Add Kafka Connect config entry"

    override fun getName(): String = "Add `$key`"

    override fun applyFix(project: com.intellij.openapi.project.Project, descriptor: ProblemDescriptor) {
        val file = descriptor.psiElement.containingFile
        val config = KConnectConfigModel.extract(file) ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

        val insertion = when (config.format) {
            KConnectConfigFormat.JSON -> appendJsonEntry(document.text, key, value)
            KConnectConfigFormat.YAML -> appendYamlEntry(document.text, key, value)
            KConnectConfigFormat.PROPERTIES -> appendPropertiesEntry(document.text, key, value)
        } ?: return

        WriteCommandAction.runWriteCommandAction(project) {
            document.setText(insertion)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

    private fun appendJsonEntry(text: String, key: String, value: String): String? {
        val trimmed = text.trimEnd()
        val closingBraceOffset = trimmed.lastIndexOf('}')
        if (closingBraceOffset < 0) {
            return null
        }

        val hasEntries = trimmed.substring(0, closingBraceOffset).contains(':')
        val indent = "  "
        val linePrefix = if (hasEntries) ",\n" else "\n"
        val quotedValue = value.replace("\"", "\\\"")
        return buildString {
            append(trimmed.substring(0, closingBraceOffset))
            append(linePrefix)
            append(indent)
            append("\"")
            append(key)
            append("\": \"")
            append(quotedValue)
            append("\"")
            append("\n}")
        }
    }

    private fun appendYamlEntry(text: String, key: String, value: String): String {
        val separator = if (text.isBlank() || text.endsWith("\n")) "" else "\n"
        return text + separator + "$key: \"$value\""
    }

    private fun appendPropertiesEntry(text: String, key: String, value: String): String {
        val separator = if (text.isBlank() || text.endsWith("\n")) "" else "\n"
        return text + separator + "$key=$value"
    }
}
