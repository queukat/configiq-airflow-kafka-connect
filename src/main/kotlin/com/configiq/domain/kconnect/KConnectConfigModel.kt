package com.configiq.domain.kconnect

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.lang.properties.psi.Property
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

private const val NAME_KEY = "name"
private const val CONNECTOR_CLASS_KEY = "connector.class"
private const val TASKS_MAX_KEY = "tasks.max"
private const val TOPICS_KEY = "topics"
private const val TOPICS_REGEX_KEY = "topics.regex"
private const val TRANSFORMS_KEY = "transforms"

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

    fun missingRequiredKeys(): List<String> = listOf(NAME_KEY, CONNECTOR_CLASS_KEY, TASKS_MAX_KEY).filter { it !in entries }

    fun declaredTransforms(): List<String> {
        val transforms = entry(TRANSFORMS_KEY)?.value ?: return emptyList()
        return transforms
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}

object KConnectConfigModel {
    private val signatureKeys =
        setOf(
            CONNECTOR_CLASS_KEY,
            TASKS_MAX_KEY,
            TOPICS_KEY,
            TOPICS_REGEX_KEY,
            TRANSFORMS_KEY,
            NAME_KEY,
        )

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
        val entries =
            rootObject.propertyList.mapNotNull { property ->
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
        val entries =
            PsiTreeUtil
                .collectElementsOfType(file, YAMLKeyValue::class.java)
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
        val entries =
            PsiTreeUtil.collectElementsOfType(file, Property::class.java).mapNotNull { property ->
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
        val hasConnectorClass = CONNECTOR_CLASS_KEY in keys
        val signatureCount = keys.count { it in signatureKeys || it.startsWith("transforms.") }
        val looksLikeKConnect =
            hasConnectorClass ||
                signatureCount >= 2 &&
                (TOPICS_KEY in keys || TOPICS_REGEX_KEY in keys || TRANSFORMS_KEY in keys)

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
