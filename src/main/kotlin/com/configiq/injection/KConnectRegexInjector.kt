package com.configiq.injection

import com.configiq.domain.kconnect.KConnectConfigModel
import com.configiq.settings.ConfigIqSettingsService
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import org.intellij.lang.regexp.RegExpLanguage
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class KConnectRegexInjector : MultiHostInjector {
    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return listOf(
            JsonStringLiteral::class.java,
            YAMLScalar::class.java,
        )
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val host = context as? PsiLanguageInjectionHost ?: return
        if (!host.isValidHost) {
            return
        }
        if (!ConfigIqSettingsService.getInstance().isKafkaConnectPackEnabled()) {
            return
        }

        val file = host.containingFile ?: return
        if (KConnectConfigModel.extract(file) == null) {
            return
        }

        val target = when (host) {
            is JsonStringLiteral -> jsonTarget(host)
            is YAMLScalar -> yamlTarget(host)
            else -> null
        } ?: return

        if (!isRegexKey(target.key)) {
            return
        }

        registrar.startInjecting(RegExpLanguage.INSTANCE)
        registrar.addPlace(null, null, host, target.range)
        registrar.doneInjecting()
    }

    private fun jsonTarget(host: JsonStringLiteral): InjectionTarget? {
        val property = host.parent as? JsonProperty ?: return null
        return InjectionTarget(
            key = property.name,
            range = ElementManipulators.getValueTextRange(host),
        )
    }

    private fun yamlTarget(host: YAMLScalar): InjectionTarget? {
        val keyValue = host.parent as? YAMLKeyValue ?: return null
        return InjectionTarget(
            key = keyValue.keyText,
            range = ElementManipulators.getValueTextRange(host),
        )
    }

    private fun isRegexKey(key: String): Boolean {
        return key == "topics.regex" || REGEX_TRANSFORM_KEY.matches(key)
    }

    private data class InjectionTarget(
        val key: String,
        val range: TextRange,
    )

    private companion object {
        val REGEX_TRANSFORM_KEY = Regex("""transforms\.[^.]+\.regex""")
    }
}
