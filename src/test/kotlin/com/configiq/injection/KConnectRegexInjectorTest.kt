package com.configiq.injection

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.JsonFileType
import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.psi.PsiLanguageInjectionHost

class KConnectRegexInjectorTest : BasePlatformTestCase() {
    fun testInjectsRegexIntoTopicsRegexField() {
        val file = myFixture.configureByText(
            JsonFileType.INSTANCE,
            """
            {
              "name": "orders-source",
              "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
              "tasks.max": "1",
              "topics.regex": "orders\\..*"
            }
            """.trimIndent(),
        )

        val rootObject = (file as JsonFile).topLevelValue as JsonObject
        val property = rootObject.findProperty("topics.regex")
        val resolvedHost = (property as JsonProperty).value as? JsonStringLiteral
            ?: throw AssertionError("Expected JsonStringLiteral value for topics.regex")

        val registrar = CapturingRegistrar()
        KConnectRegexInjector().getLanguagesToInject(registrar, resolvedHost)

        assertEquals("RegExp", registrar.language?.id)
        assertSame(resolvedHost, registrar.host)
        assertNotNull(registrar.range)
    }

    private class CapturingRegistrar : MultiHostRegistrar {
        var language: Language? = null
        var host: PsiLanguageInjectionHost? = null
        var range: TextRange? = null

        override fun startInjecting(language: Language): MultiHostRegistrar {
            this.language = language
            return this
        }

        override fun addPlace(
            prefix: String?,
            suffix: String?,
            host: PsiLanguageInjectionHost,
            rangeInsideHost: TextRange,
        ): MultiHostRegistrar {
            this.host = host
            this.range = rangeInsideHost
            return this
        }

        override fun doneInjecting() = Unit
    }
}
