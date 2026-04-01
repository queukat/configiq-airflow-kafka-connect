package com.configiq.inspection

import com.intellij.json.JsonFileType
import com.intellij.lang.properties.PropertiesFileType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.yaml.YAMLFileType

class KConnectConfigInspectionTest : BasePlatformTestCase() {
    fun testHighlightsTopicsConflictAndRemovesTopicsRegex() {
        myFixture.enableInspections(KConnectJsonInspection())
        myFixture.configureByText(
            JsonFileType.INSTANCE,
            """
            {
              "name": "orders-source",
              "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
              "tasks.max": "1",
              <warning descr="`topics` conflicts with `topics.regex`.">"topics": "orders"</warning>,
              <caret><warning descr="`topics.regex` conflicts with `topics`.">"topics.regex": "orders.*"</warning>
            }
            """.trimIndent(),
        )

        myFixture.checkHighlighting()

        val quickFix = myFixture.findSingleIntention("Remove `topics.regex`")
        myFixture.launchAction(quickFix)
        myFixture.checkResult(
            """
            {
              "name": "orders-source",
              "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
              "tasks.max": "1",
              "topics": "orders"
            }
            """.trimIndent(),
        )
    }

    fun testHighlightsMissingTransformTypeAndAddsPlaceholder() {
        myFixture.enableInspections(KConnectYamlInspection())
        myFixture.configureByText(
            YAMLFileType.YML,
            """
            name: audit-sink
            connector.class: io.confluent.connect.s3.S3SinkConnector
            tasks.max: "1"
            <warning descr="Transform alias `maskTopic` is missing `transforms.maskTopic.type`."><caret>transforms</warning>: maskTopic
            transforms.maskTopic.regex: "audit\\.(.*)"
            """.trimIndent(),
        )

        myFixture.checkHighlighting()

        val quickFix = myFixture.findSingleIntention("Add `transforms.maskTopic.type`")
        myFixture.launchAction(quickFix)
        myFixture.checkResult(
            """
            name: audit-sink
            connector.class: io.confluent.connect.s3.S3SinkConnector
            tasks.max: "1"
            transforms: maskTopic
            transforms.maskTopic.regex: "audit\\.(.*)"
            transforms.maskTopic.type: "<transform class>"
            """.trimIndent(),
        )
    }

    fun testHighlightsMissingTasksMaxInPropertiesFile() {
        myFixture.enableInspections(KConnectPropertiesInspection())
        myFixture.configureByText(
            PropertiesFileType.INSTANCE,
            """
            <warning descr="Missing required Kafka Connect field `tasks.max`.">name</warning>=inventory-source
            connector.class=io.debezium.connector.postgresql.PostgresConnector
            topics.regex=inventory\..*
            """.trimIndent(),
        )

        myFixture.checkHighlighting()
    }
}
