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

    testImplementation(kotlin("test"))

    // Playwright
    testImplementation("com.microsoft.playwright:playwright:1.44.0")

    // Coroutines for async testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Logging
    testImplementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    testImplementation("ch.qos.logback:logback-classic:1.4.14")

    // JSON parsing for test data
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Parallel execution
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

// Custom task to run only mobile tests
tasks.register<Test>("mobileTests") {
    useJUnitPlatform()
}

// Custom task to run only desktop tests
tasks.register<Test>("desktopTests") {
    useJUnitPlatform()
}

// Install Playwright browsers
tasks.register<Exec>("installPlaywright") {
    commandLine("npx", "playwright", "install", "--with-deps")
}

allure {
    version.set("2.24.0")
}
