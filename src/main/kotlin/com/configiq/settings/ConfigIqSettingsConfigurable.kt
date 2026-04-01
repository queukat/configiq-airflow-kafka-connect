package com.configiq.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.components.JBCheckBox
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class ConfigIqSettingsConfigurable : SearchableConfigurable {
    private var panel: JPanel? = null
    private var airflowCheckBox: JBCheckBox? = null
    private var kafkaConnectCheckBox: JBCheckBox? = null

    override fun getId(): String = "com.configiq.airflowkconnect.settings"

    override fun getDisplayName(): String = "ConfigIQ"

    override fun createComponent(): JComponent {
        if (panel == null) {
            val content = JPanel(GridLayout(0, 1, 0, 8))
            content.add(JLabel("Enable only the packs that improve the current authoring workflow."))

            airflowCheckBox = JBCheckBox("Enable Airflow authoring pack")
            kafkaConnectCheckBox = JBCheckBox("Enable Kafka Connect authoring pack")

            content.add(airflowCheckBox)
            content.add(kafkaConnectCheckBox)

            panel = JPanel(BorderLayout())
            panel!!.add(content, BorderLayout.NORTH)
        }

        reset()
        return panel!!
    }

    override fun isModified(): Boolean {
        val settings = ConfigIqSettingsService.getInstance()
        return airflowCheckBox?.isSelected != settings.isAirflowPackEnabled() ||
            kafkaConnectCheckBox?.isSelected != settings.isKafkaConnectPackEnabled()
    }

    override fun apply() {
        val settings = ConfigIqSettingsService.getInstance()
        settings.setAirflowPackEnabled(airflowCheckBox?.isSelected ?: true)
        settings.setKafkaConnectPackEnabled(kafkaConnectCheckBox?.isSelected ?: true)
    }

    override fun reset() {
        val settings = ConfigIqSettingsService.getInstance()
        airflowCheckBox?.isSelected = settings.isAirflowPackEnabled()
        kafkaConnectCheckBox?.isSelected = settings.isKafkaConnectPackEnabled()
    }

    override fun disposeUIResources() {
        panel = null
        airflowCheckBox = null
        kafkaConnectCheckBox = null
    }
}
