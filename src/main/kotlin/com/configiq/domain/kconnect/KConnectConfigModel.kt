package com.configiq.domain.kconnect

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.lang.properties.psi.Property
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

enum class KConnectConfigFormat {
    JSON,
    YAML,
    PROPERTIES,
}

data class KConnectEntry(
    val key: String,
    val value: String,
    val element: PsiElement,
)

data class KConnectConfig(
    val file: PsiFile,
    val format: KConnectConfigFormat,
    val entries: Map<String, KConnectEntry>,
) {
    val anchor: PsiElement
        get() = entries.values.firstOrNull()?.element ?: file

    fun entry(key: String): KConnectEntry? = entries[key]

    fun missingRequiredKeys(): List<String> {
        return listOf("name", "connector.class", "tasks.max").filter { it !in entries }
    }

    fun declaredTransforms(): List<String> {
        val transforms = entry("transforms")?.value ?: return emptyList()
        return transforms
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}

object KConnectConfigModel {
    private val signatureKeys = setOf("connector.class", "tasks.max", "topics", "topics.regex", "transforms", "name")

    fun extract(file: PsiFile): KConnectConfig? {
        val jsonConfig = fromJson(file as? JsonFile)
        if (jsonConfig != null) {
            return jsonConfig
        }

        val yamlConfig = fromYaml(file)
        if (yamlConfig != null) {
            return yamlConfig
        }

        return fromProperties(file)
    }

    private fun fromJson(file: JsonFile?): KConnectConfig? {
        file ?: return null
        val rootObject = file.topLevelValue as? JsonObject ?: return null
        val entries = rootObject.propertyList.mapNotNull { property ->
            val value = property.value ?: return@mapNotNull null
            KConnectEntry(
                key = property.name,
                value = normalizeScalar(value.text),
                element = property,
            )
        }
        return buildConfig(file, KConnectConfigFormat.JSON, entries)
    }

    private fun fromYaml(file: PsiFile): KConnectConfig? {
        val entries = PsiTreeUtil.collectElementsOfType(file, YAMLKeyValue::class.java)
            .filter { it.parent is YAMLMapping }
            .mapNotNull { keyValue ->
            val key = keyValue.keyText.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val value = keyValue.valueText.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            KConnectEntry(
                key = key,
                value = normalizeScalar(value),
                element = keyValue.firstChild ?: keyValue,
            )
        }
        return buildConfig(file, KConnectConfigFormat.YAML, entries)
    }

    private fun fromProperties(file: PsiFile): KConnectConfig? {
        val entries = PsiTreeUtil.collectElementsOfType(file, Property::class.java).mapNotNull { property ->
            val key = property.key ?: return@mapNotNull null
            val value = property.value ?: return@mapNotNull null
            KConnectEntry(
                key = key,
                value = value,
                // Highlight the whole property line for missing-key warnings.
                element = property,
            )
        }
        return buildConfig(file, KConnectConfigFormat.PROPERTIES, entries)
    }

    private fun buildConfig(
        file: PsiFile,
        format: KConnectConfigFormat,
        entries: List<KConnectEntry>,
    ): KConnectConfig? {
        if (entries.isEmpty()) {
            return null
        }

        val byKey = entries.associateBy { it.key }
        val keys = byKey.keys
        val hasConnectorClass = "connector.class" in keys
        val signatureCount = keys.count { it in signatureKeys || it.startsWith("transforms.") }
        val looksLikeKConnect = hasConnectorClass ||
            signatureCount >= 2 && ("topics" in keys || "topics.regex" in keys || "transforms" in keys)

        if (!looksLikeKConnect) {
            return null
        }

        return KConnectConfig(file = file, format = format, entries = byKey)
    }

    private fun normalizeScalar(rawValue: String): String {
        val trimmed = rawValue.trim()
        if (trimmed.length >= 2 && trimmed.first() == '"' && trimmed.last() == '"') {
            return trimmed.substring(1, trimmed.length - 1)
        }
        return trimmed
    }
}
