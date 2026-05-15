package com.configiq.test

import java.util.concurrent.ConcurrentHashMap
import java.util.prefs.AbstractPreferences
import java.util.prefs.Preferences
import java.util.prefs.PreferencesFactory

class InMemoryPreferencesFactory : PreferencesFactory {
    private val userRoot = InMemoryPreferences(null, "")
    private val systemRoot = InMemoryPreferences(null, "")

    override fun userRoot(): Preferences = userRoot

    override fun systemRoot(): Preferences = systemRoot
}

private class InMemoryPreferences(
    parent: AbstractPreferences?,
    name: String,
) : AbstractPreferences(parent, name) {
    private val values = ConcurrentHashMap<String, String>()
    private val children = ConcurrentHashMap<String, InMemoryPreferences>()

    override fun putSpi(
        key: String,
        value: String,
    ) {
        values[key] = value
    }

    override fun getSpi(key: String): String? = values[key]

    override fun removeSpi(key: String) {
        values.remove(key)
    }

    override fun removeNodeSpi() {
        values.clear()
        children.clear()
    }

    override fun keysSpi(): Array<String> = values.keys().toList().toTypedArray()

    override fun childrenNamesSpi(): Array<String> = children.keys().toList().toTypedArray()

    override fun childSpi(name: String): AbstractPreferences =
        children.computeIfAbsent(name) { childName ->
            InMemoryPreferences(this, childName)
        }

    override fun syncSpi() = Unit

    override fun flushSpi() = Unit
}
