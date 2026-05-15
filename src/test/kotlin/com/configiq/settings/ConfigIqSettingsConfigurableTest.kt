package com.configiq.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.components.JBCheckBox
import java.awt.Component
import java.awt.Container

class ConfigIqSettingsConfigurableTest : BasePlatformTestCase() {
    override fun tearDown() {
        try {
            ConfigIqSettingsService.getInstance().loadState(ConfigIqSettingsService.State())
        } finally {
            super.tearDown()
        }
    }

    fun testAppliesAndResetsPackCheckboxes() {
        val settings = ConfigIqSettingsService.getInstance()
        settings.loadState(
            ConfigIqSettingsService.State().apply {
                airflowPackEnabled = false
                kafkaConnectPackEnabled = true
            },
        )

        val configurable = ConfigIqSettingsConfigurable()
        val component = configurable.createComponent()
        val airflowCheckBox = findCheckBox(component, "Enable Airflow authoring pack")
        val kafkaConnectCheckBox = findCheckBox(component, "Enable Kafka Connect authoring pack")

        assertFalse(airflowCheckBox.isSelected)
        assertTrue(kafkaConnectCheckBox.isSelected)
        assertFalse(configurable.isModified())

        airflowCheckBox.isSelected = true
        kafkaConnectCheckBox.isSelected = false

        assertTrue(configurable.isModified())

        configurable.apply()

        assertTrue(settings.isAirflowPackEnabled())
        assertFalse(settings.isKafkaConnectPackEnabled())
        assertFalse(configurable.isModified())

        settings.setAirflowPackEnabled(false)
        settings.setKafkaConnectPackEnabled(true)

        configurable.reset()

        assertFalse(airflowCheckBox.isSelected)
        assertTrue(kafkaConnectCheckBox.isSelected)

        configurable.disposeUIResources()
        assertNotSame(component, configurable.createComponent())
    }

    private fun findCheckBox(
        component: Component,
        text: String,
    ): JBCheckBox {
        if (component is JBCheckBox && component.text == text) {
            return component
        }

        if (component is Container) {
            component.components.forEach { child ->
                val match = runCatching { findCheckBox(child, text) }.getOrNull()
                if (match != null) {
                    return match
                }
            }
        }

        throw AssertionError("Unable to find checkbox '$text'.")
    }
}
