package com.configiq.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(name = "ConfigIqSettings", storages = [Storage("configiq-settings.xml")])
class ConfigIqSettingsService : PersistentStateComponent<ConfigIqSettingsService.State> {
    class State {
        var airflowPackEnabled: Boolean = true
        var kafkaConnectPackEnabled: Boolean = true
    }

    private var currentState = State()

    override fun getState(): State = currentState

    override fun loadState(state: State) {
        currentState = state
    }

    fun isAirflowPackEnabled(): Boolean = currentState.airflowPackEnabled

    fun isKafkaConnectPackEnabled(): Boolean = currentState.kafkaConnectPackEnabled

    fun setAirflowPackEnabled(enabled: Boolean) {
        currentState.airflowPackEnabled = enabled
    }

    fun setKafkaConnectPackEnabled(enabled: Boolean) {
        currentState.kafkaConnectPackEnabled = enabled
    }

    companion object {
        fun getInstance(): ConfigIqSettingsService = service()
    }
}
