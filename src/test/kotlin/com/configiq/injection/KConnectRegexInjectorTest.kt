package com.configiq.injection

import com.intellij.json.JsonFileType
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.properties.PropertiesFileType
import com.intellij.lang.properties.psi.Property
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KConnectRegexInjectorTest : BasePlatformTestCase() {
    fun testInjectsRegexIntoTopicsRegexField() {
        val file =
            myFixture.configureByText(
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
        val resolvedHost =
            (property as JsonProperty).value as? JsonStringLiteral
                ?: throw AssertionError("Expected JsonStringLiteral value for topics.regex")

        val registrar = CapturingRegistrar()
        KConnectRegexInjector().getLanguagesToInject(registrar, resolvedHost)

        assertEquals("RegExp", registrar.language?.id)
        assertSame(resolvedHost, registrar.host)
        assertNotNull(registrar.range)
    }

    fun testInjectsRegexIntoPropertiesTopicsRegexField() {
        val file =
            myFixture.configureByText(
                PropertiesFileType.INSTANCE,
                """
                name=inventory-source
                connector.class=io.debezium.connector.postgresql.PostgresConnector
                tasks.max=1
                topics.regex=inventory\..*
                """.trimIndent(),
            )

        val property = findProperty(file, "topics.regex")

        val registrar = CapturingRegistrar()
        KConnectRegexInjector().getLanguagesToInject(registrar, property)

        assertEquals("RegExp", registrar.language?.id)
        assertSame(property, registrar.host)
        assertEquals("inventory\\..*", property.text.substring(registrar.range!!.startOffset, registrar.range!!.endOffset))
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

    private fun findProperty(
        file: com.intellij.psi.PsiFile,
        key: String,
    ): Property =
        PsiTreeUtil
            .collectElementsOfType(file, Property::class.java)
            .single { it.key == key }
}
