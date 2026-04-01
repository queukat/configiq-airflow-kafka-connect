package com.configiq.quickfix

import com.configiq.domain.kconnect.KConnectConfigFormat
import com.configiq.domain.kconnect.KConnectConfigModel
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager

class RemoveKConnectEntryQuickFix(
    private val key: String,
) : LocalQuickFix {
    override fun getFamilyName(): String = "Remove Kafka Connect config entry"

    override fun getName(): String = "Remove `$key`"

    override fun applyFix(project: com.intellij.openapi.project.Project, descriptor: ProblemDescriptor) {
        val file = descriptor.psiElement.containingFile
        val config = KConnectConfigModel.extract(file) ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
        val remainingEntries = config.entries.values.filter { it.key != key }
        val replacement = when (config.format) {
            KConnectConfigFormat.JSON -> serializeJson(remainingEntries)
            KConnectConfigFormat.YAML -> serializeYaml(remainingEntries)
            KConnectConfigFormat.PROPERTIES -> serializeProperties(remainingEntries)
        }

        WriteCommandAction.runWriteCommandAction(project) {
            document.setText(replacement)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

    private fun serializeJson(entries: List<com.configiq.domain.kconnect.KConnectEntry>): String {
        if (entries.isEmpty()) {
            return "{}"
        }

        return entries.joinToString(
            separator = ",\n",
            prefix = "{\n",
            postfix = "\n}",
        ) { entry ->
            val escapedValue = entry.value.replace("\"", "\\\"")
            "  \"${entry.key}\": \"$escapedValue\""
        }
    }

    private fun serializeYaml(entries: List<com.configiq.domain.kconnect.KConnectEntry>): String {
        return entries.joinToString("\n") { entry ->
            "${entry.key}: ${renderYamlValue(entry.value)}"
        }
    }

    private fun serializeProperties(entries: List<com.configiq.domain.kconnect.KConnectEntry>): String {
        return entries.joinToString("\n") { entry ->
            "${entry.key}=${entry.value}"
        }
    }

    private fun renderYamlValue(value: String): String {
        return if (value.any { it.isWhitespace() || it == ':' || it == '"' }) {
            "\"${value.replace("\"", "\\\"")}\""
        } else {
            value
        }
    }
}
