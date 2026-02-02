import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("io.qameta.allure") version "2.12.0"
}

group = "com.deepholistics"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val agent: Configuration by configurations.creating

dependencies {

    testImplementation(kotlin("test"))

    // Playwright
    testImplementation("com.microsoft.playwright:playwright:1.49.0")

    // Coroutines for async testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Logging
    testImplementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // JSON parsing for test data
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Allure
    testImplementation("io.qameta.allure:allure-junit5:2.29.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    agent("org.aspectj:aspectjweaver:1.9.22")
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
    
    // Attach AspectJ Agent for Allure steps and attachments
    jvmArgs("-javaagent:${agent.singleFile}")

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

// Custom task to run only login tests
tasks.register<Test>("loginTests") {
    useJUnitPlatform()
    filter {
        includeTestsMatching("login.*")
    }
}

// Install Playwright browsers
tasks.register<Exec>("installPlaywright") {
    commandLine("npx", "playwright", "install", "--with-deps")
}

allure {
    version.set("2.29.0")
    adapter {
        frameworks {
            junit5 {
                adapterVersion.set("2.29.0")
            }
        }
    }
}

// Allure v3 tasks using npx
tasks.register<Exec>("allure3Report") {
    group = "verification"
    description = "Generates Allure Report v3"
    
    val env = project.findProperty("environment") ?: "Local"
    
    doFirst {
        val resultsDir = file("build/allure-results")
        if (!resultsDir.exists()) {
            throw GradleException("Allure results directory 'build/allure-results' does not exist. Run tests first.")
        }
        val envFile = resultsDir.resolve("environment.properties")
        envFile.writeText("Environment=$env")
        
        // Clean up old report
        val reportDir = file("build/allure-report-v3")
        if (reportDir.exists()) {
            reportDir.deleteRecursively()
        }
    }
    
    commandLine("npx", "allure", "generate", "--config", "allurerc.mjs", "build/allure-results", "-o", "build/allure-report-v3")
}

tasks.register<Exec>("allure3Serve") {
    group = "verification"
    description = "Serves Allure Report v3"
    commandLine("npx", "allure", "serve", "build/allure-results")
}