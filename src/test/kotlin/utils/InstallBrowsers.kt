package utils

import com.microsoft.playwright.Playwright
import com.microsoft.playwright.BrowserType

/**
 * Utility to install Playwright browsers.
 * This script triggers Playwright to download browsers by attempting to launch them.
 * Run this via: ./gradlew installPlaywright
 */
fun main() {
    println("Installing Playwright browsers...")
    println("This may take a few minutes on first run...")
    
    var success = true
    
    try {
        Playwright.create().use { playwright ->
            // Install Chromium
            try {
                println("Installing Chromium...")
                val browser = playwright.chromium().launch(
                    BrowserType.LaunchOptions().setHeadless(true)
                )
                browser.close()
                println("✓ Chromium installed successfully")
            } catch (e: Exception) {
                println("✗ Failed to install Chromium: ${e.message}")
                success = false
            }
        }
        
        if (success) {
            println("\n✓ All browsers installed successfully!")
            println("You can now run your tests with: ./gradlew test")
        } else {
            println("\n⚠ Some browsers failed to install.")
            println("You may need to:")
            println("1. Check your internet connection")
            println("2. Update Playwright version")
            println("3. Try again later (CDN issues may be temporary)")
            System.exit(1)
        }
    } catch (e: Exception) {
        println("Error installing browsers: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
}

