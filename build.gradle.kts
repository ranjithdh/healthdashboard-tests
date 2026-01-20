import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("io.qameta.allure") version "2.11.2"
}

group = "com.deepholistics"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin test library
    testImplementation(kotlin("test"))

    // Playwright
    testImplementation("com.microsoft.playwright:playwright:1.44.0")

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // Coroutines
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Logging
    testImplementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    testImplementation("ch.qos.logback:logback-classic:1.4.14")

    // JSON parsing for test data
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

// Playwright browser installation
tasks.register<Exec>("installPlaywright") {
    commandLine("npx", "playwright", "install", "--with-deps")
}

// Test configuration
tasks.withType<Test> {
    useJUnitPlatform()

/*
    // ✅ CLEAR allure-results BEFORE tests
    doFirst {
        delete(layout.buildDirectory.dir("allure-results"))
    }
*/

    systemProperty("buildDir", layout.buildDirectory.get().asFile.absolutePath)

    // Allure results folder
    systemProperty("allure.results.directory", "$buildDir/allure-results")

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

// Allure plugin configuration
allure {
    version.set("2.24.0")
    adapter {
        autoconfigure.set(true)
        aspectjWeaver.set(true)
    }
}
