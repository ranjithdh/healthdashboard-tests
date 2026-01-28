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

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // Coroutines
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Logging
    testImplementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // JSON parsing for test data
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // JUnit Params
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")

    //Allure
    testImplementation("io.qameta.allure:allure-junit5:2.25.0")
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


    val env = System.getProperty("env") ?: "stg"

    doFirst {
        val resultsDir = layout.buildDirectory.dir("allure-results").get().asFile
        resultsDir.mkdirs()

        val envFile = File(resultsDir, "environment.properties")
        envFile.writeText(
            """
            Environment=$env
            BaseURL=${if (env == "prod") "https://api.prod.com" else "https://api.stg.com"}
            Platform=Web
            Browser=Chromium
            Version=2.1.27
            """.trimIndent()
        )
    }

    // ✅ CLEAR allure-results BEFORE tests
    doFirst {
        delete(layout.buildDirectory.dir("allure-results"))
    }

    systemProperty(
        "allure.results.directory",
        layout.buildDirectory.dir("allure-results").get().asFile.absolutePath
    )

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
    version.set("2.25.0")

    adapter {
        autoconfigure.set(true)
    }
}
