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

    // JSON parsing for test data
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // JUnit Params
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
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
    filter {
        includeTestsMatching("mobileView.*")
    }
}

// Custom task to run only website tests
tasks.register<Test>("websiteTests") {
    useJUnitPlatform()
    filter {
        includeTestsMatching("website.*")
    }
}

// Custom task to run only desktop/app tests
tasks.register<Test>("appTests") {
    useJUnitPlatform()
    filter {
        includeTestsMatching("login.*")
        includeTestsMatching("healthdata.*")
        includeTestsMatching("profile.*")
        includeTestsMatching("symptoms.*")
        includeTestsMatching("webView.*")
    }
}

// Install Playwright browsers
tasks.register<Exec>("installPlaywright") {
    commandLine("npx", "playwright", "install", "--with-deps")
}

allure {
    version.set("2.24.0")
}