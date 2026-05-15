package com.configiq.inspection

import com.configiq.domain.kconnect.KConnectConfig
import com.configiq.domain.kconnect.KConnectConfigModel
import com.configiq.quickfix.AddKConnectEntryQuickFix
import com.configiq.quickfix.RemoveKConnectEntryQuickFix
import com.configiq.settings.ConfigIqSettingsService
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile

abstract class KConnectConfigInspection : LocalInspectionTool() {
    override fun checkFile(
        file: PsiFile,
        manager: InspectionManager,
        isOnTheFly: Boolean,
    ): Array<ProblemDescriptor> {
        if (!ConfigIqSettingsService.getInstance().isKafkaConnectPackEnabled()) {
            return emptyArray()
        }

        val config = KConnectConfigModel.extract(file) ?: return emptyArray()
        val problems = mutableListOf<ProblemDescriptor>()
        registerMissingRequiredKeys(problems, manager, config, isOnTheFly)
        registerTopicConflict(problems, manager, config, isOnTheFly)
        registerMissingTransformTypes(problems, manager, config, isOnTheFly)
        return problems.toTypedArray()
    }

    private fun registerMissingRequiredKeys(
        problems: MutableList<ProblemDescriptor>,
        manager: InspectionManager,
        config: KConnectConfig,
        isOnTheFly: Boolean,
    ) {
        config.missingRequiredKeys().forEach { missingKey ->
            val quickFix =
                when (missingKey) {
                    "tasks.max" -> AddKConnectEntryQuickFix("tasks.max", "1")
                    "name" -> AddKConnectEntryQuickFix("name", "<connector-name>")
                    "connector.class" -> AddKConnectEntryQuickFix("connector.class", "<connector-class>")
                    else -> null
                }

            problems +=
                manager.createProblemDescriptor(
                    config.anchor,
                    "Missing required Kafka Connect field `$missingKey`.",
                    isOnTheFly,
                    quickFix?.let { arrayOf(it) } ?: emptyArray<LocalQuickFix>(),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                )
        }
    }

    private fun registerTopicConflict(
        problems: MutableList<ProblemDescriptor>,
        manager: InspectionManager,
        config: KConnectConfig,
        isOnTheFly: Boolean,
    ) {
        val topics = config.entry("topics") ?: return
        val topicsRegex = config.entry("topics.regex") ?: return

        problems +=
            manager.createProblemDescriptor(
                topics.element,
                "`topics` conflicts with `topics.regex`.",
                isOnTheFly,
                arrayOf(RemoveKConnectEntryQuickFix("topics")),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            )
        problems +=
            manager.createProblemDescriptor(
                topicsRegex.element,
                "`topics.regex` conflicts with `topics`.",
                isOnTheFly,
                arrayOf(RemoveKConnectEntryQuickFix("topics.regex")),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            )
    }

    private fun registerMissingTransformTypes(
        problems: MutableList<ProblemDescriptor>,
        manager: InspectionManager,
        config: KConnectConfig,
        isOnTheFly: Boolean,
    ) {
        config.declaredTransforms().forEach { alias ->
            val typeKey = "transforms.$alias.type"
            if (config.entry(typeKey) != null) {
                return@forEach
            }

            problems +=
                manager.createProblemDescriptor(
                    config.entry("transforms")?.element ?: config.anchor,
                    "Transform alias `$alias` is missing `$typeKey`.",
                    isOnTheFly,
                    arrayOf(AddKConnectEntryQuickFix(typeKey, "<transform class>")),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                )
        }
    }
}

class KConnectJsonInspection : KConnectConfigInspection()

class KConnectYamlInspection : KConnectConfigInspection()

class KConnectPropertiesInspection : KConnectConfigInspection()
