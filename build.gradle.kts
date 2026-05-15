import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    kotlin("jvm") version "2.1.21"
    id("org.jetbrains.intellij.platform") version "2.12.0"
    id("dev.detekt") version "2.0.0-alpha.3"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("org.sonarqube") version "7.3.0.8198"
    id("org.jetbrains.kotlinx.kover") version "0.9.8"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(17)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    config.setFrom(files("config/detekt/detekt.yml"))
    basePath.set(projectDir)
}

ktlint {
    filter {
        exclude("**/build/**")
        exclude("**/.gradle/**")
        exclude("**/.gradle-user-home/**")
        exclude("**/.kotlin/**")
        exclude("**/intellijPlatform/**")
    }
}

sonar {
    properties {
        property("sonar.projectKey", providers.gradleProperty("sonarProjectKey").orElse(rootProject.name).get())
        property("sonar.projectName", "ConfigIQ Ops")
        property("sonar.sourceEncoding", "UTF-8")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory
                .file("reports/kover/kover.xml")
                .get()
                .asFile
                .absolutePath,
        )
        property(
            "sonar.exclusions",
            listOf(
                "**/build/**",
                "**/.gradle/**",
                "**/.gradle-user-home/**",
                "**/.kotlin/**",
                "**/intellijPlatform/**",
                "assets/**",
                "docs/**",
                "samples/**",
                "scripts/**",
            ).joinToString(","),
        )
    }
}

kover {
    reports {
        total {
            xml {
                xmlFile = file("build/reports/kover/kover.xml")
            }
        }
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:${providers.gradleProperty("junitVersion").get()}")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:${providers.gradleProperty("junitVintageVersion").get()}")

    intellijPlatform {
        pycharm(providers.gradleProperty("platformVersion"))
        bundledPlugin("Pythonid")
        bundledPlugin("com.intellij.properties")
        bundledPlugin("org.intellij.intelliLang")
        bundledPlugin("org.jetbrains.plugins.yaml")
        bundledPlugin("com.intellij.modules.json")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.configiq.airflowkconnect"
        name = "ConfigIQ Ops"
        version = providers.gradleProperty("pluginVersion")

        description =
            """
            <p>ConfigIQ Ops is a narrow authoring assistant for Airflow DAG schedules and Kafka Connect configs.</p>
            <ul>
                <li>Validates Airflow schedule strings and previews the next runs.</li>
                <li>Flags high-value Kafka Connect conflicts and missing required keys.</li>
                <li>Adds focused quick fixes and RegExp injection for JSON, YAML, and properties regex fields.</li>
            </ul>
            """.trimIndent()

        changeNotes =
            """
            <p>Kafka Connect properties regex support and release hardening.</p>
            <ul>
                <li>Added RegExp injection for Kafka Connect <code>.properties</code> regex fields.</li>
                <li>Covered <code>topics.regex</code> properties injection with a regression test.</li>
                <li>Added <code>ktlintCheck</code> and <code>detekt</code> to the documented release gate.</li>
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
            create(IntelliJPlatformType.IntellijIdeaUltimate, "261.23567.71")
        }
    }

    publishing {
        token = providers.gradleProperty("intellijPlatformPublishingToken")
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

    withType<Detekt>().configureEach {
        jvmTarget.set("17")
    }

    withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget.set("17")
    }

    named("sonar") {
        dependsOn("koverXmlReport")
    }
}
