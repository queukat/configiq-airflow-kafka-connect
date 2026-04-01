import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    kotlin("jvm") version "2.1.21"
    id("org.jetbrains.intellij.platform") version "2.12.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.12.2")

    intellijPlatform {
        pycharm(providers.gradleProperty("platformVersion"))
        bundledPlugin("PythonCore")
        bundledPlugin("com.intellij.properties")
        bundledPlugin("org.intellij.intelliLang")
        bundledPlugin("org.jetbrains.plugins.yaml")
        bundledModule("com.intellij.modules.json")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.configiq.airflowkconnect"
        name = "ConfigIQ Ops"
        version = providers.gradleProperty("pluginVersion")

        description = """
            <p>ConfigIQ Ops is a narrow authoring assistant for Airflow DAG schedules and Kafka Connect configs.</p>
            <ul>
                <li>Validates Airflow schedule strings and previews the next runs.</li>
                <li>Flags high-value Kafka Connect conflicts and missing required keys.</li>
                <li>Adds focused quick fixes and RegExp injection for JSON and YAML regex fields.</li>
            </ul>
        """.trimIndent()

        changeNotes = """
            <p>Compatibility and release-prep patch.</p>
            <ul>
                <li>Replaced experimental Python AST usage in Airflow schedule matching with stable PSI APIs.</li>
                <li>Added regression tests to prevent verifier warnings from returning.</li>
                <li>Kept the existing Airflow and Kafka Connect MVP behavior unchanged.</li>
            </ul>
        """.trimIndent()

        ideaVersion {
            sinceBuild = providers.gradleProperty("platformSinceBuild")
        }

        vendor {
            name = "ConfigIQ"
            email = "75810528+queukat@users.noreply.github.com"
            url = "https://github.com/queukat"
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    named("buildSearchableOptions") {
        enabled = false
    }

    named("prepareJarSearchableOptions") {
        enabled = false
    }

    named("jarSearchableOptions") {
        enabled = false
    }

    wrapper {
        gradleVersion = "9.2.1"
    }

    test {
        useJUnitPlatform()
        systemProperty("java.util.prefs.PreferencesFactory", "com.configiq.test.InMemoryPreferencesFactory")
    }
}
