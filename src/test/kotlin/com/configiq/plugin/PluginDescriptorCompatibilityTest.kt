package com.configiq.plugin

import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PluginDescriptorCompatibilityTest {
    @Test
    fun mainDescriptorMakesPythonDependencyOptional() {
        val mainDescriptor = parseDescriptor("plugin.xml")
        val pythonDependency = findDependsElement(mainDescriptor, "com.intellij.modules.python")

        assertNotNull(pythonDependency)
        assertEquals("true", pythonDependency.getAttribute("optional"))
        assertEquals(
            "com.configiq.airflowkconnect-with-python.xml",
            pythonDependency.getAttribute("config-file"),
        )
    }

    @Test
    fun mainDescriptorKeepsPythonExtensionsOutOfAlwaysLoadedSection() {
        val mainDescriptor = parseDescriptor("plugin.xml")

        assertFalse(hasInspection(mainDescriptor, "AirflowInvalidSchedule"))
        assertFalse(
            hasIntention(mainDescriptor, "com.configiq.preview.PreviewAirflowScheduleIntention"),
        )
    }

    @Test
    fun optionalPythonDescriptorRegistersAirflowFeatures() {
        val pythonDescriptor = parseDescriptor("com.configiq.airflowkconnect-with-python.xml")

        assertTrue(hasInspection(pythonDescriptor, "AirflowInvalidSchedule"))
        assertTrue(
            hasIntention(pythonDescriptor, "com.configiq.preview.PreviewAirflowScheduleIntention"),
        )
    }

    private fun parseDescriptor(path: String): Document {
        val descriptorPath = Path.of(System.getProperty("user.dir"), "src", "main", "resources", "META-INF", path)
        if (!Files.exists(descriptorPath)) {
            throw AssertionError("Missing descriptor resource: $descriptorPath")
        }
        Files.newInputStream(descriptorPath).use { descriptorStream ->
            val factory = DocumentBuilderFactory.newInstance()
            return factory.newDocumentBuilder().parse(descriptorStream)
        }
    }

    private fun findDependsElement(
        document: Document,
        dependencyId: String,
    ): Element? {
        val depends = document.getElementsByTagName("depends")
        for (index in 0 until depends.length) {
            val node = depends.item(index) as? Element ?: continue
            if (node.textContent.trim() == dependencyId) {
                return node
            }
        }
        return null
    }

    private fun hasInspection(
        document: Document,
        shortName: String,
    ): Boolean {
        val inspections = document.getElementsByTagName("localInspection")
        for (index in 0 until inspections.length) {
            val node = inspections.item(index) as? Element ?: continue
            if (node.getAttribute("shortName") == shortName) {
                return true
            }
        }
        return false
    }

    private fun hasIntention(
        document: Document,
        className: String,
    ): Boolean {
        val intentions = document.getElementsByTagName("intentionAction")
        for (index in 0 until intentions.length) {
            val node = intentions.item(index) as? Element ?: continue
            val classNameNodes = node.getElementsByTagName("className")
            for (classIndex in 0 until classNameNodes.length) {
                if (classNameNodes.item(classIndex).textContent.trim() == className) {
                    return true
                }
            }
        }
        return false
    }
}
