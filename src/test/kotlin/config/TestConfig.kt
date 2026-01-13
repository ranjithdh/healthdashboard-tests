package config

import com.microsoft.playwright.BrowserType


object TestConfig {

    object Urls {
        const val BASE_URL: String = "https://app.stg.deepholistics.com/"
        const val DIAGNOSTICS_URL: String = "https://app.stg.deepholistics.com/diagnostics"
        const val LAB_TEST_API_URL: String = "https://api.stg.dh.deepholistics.com/v4/human-token/lab-test"

        const val LOGIN_URL = "${BASE_URL}login"
        const val DIAGNOSTICS_PATH = "https://app.stg.deepholistics.com/diagnostics"

        const val HOME_PAGE_URL = "$BASE_URL/home"
        const val PROFILE_URL = "$BASE_URL/profile"


        const val WEBSITE_BASE_URL: String = "https://www.deepholistics.com/"
        const val HOW_IT_WORKS: String = "${WEBSITE_BASE_URL}how-it-works"
        const val WHAT_WE_TEST: String = "${WEBSITE_BASE_URL}what-we-test"
        const val OUR_WHY: String = "${WEBSITE_BASE_URL}our-why"
        const val FAQ: String = "${WEBSITE_BASE_URL}faq"
        const val ALL_TEST: String = "${WEBSITE_BASE_URL}all-tests"

        const val ALLERGY_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/allergies"
        const val GUT_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/gut"
        const val STRESS_CORTISOL_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/stress-and-cortisol"
        const val GENE_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/gene"


        const val SIGNUP_VIA_WEBSITE = "https://app.deepholistics.com/login?mode=signup&utm_source=direct&via=website"
        const val LOGIN_VIA_WEBSITE = "https://app.deepholistics.com/login?utm_source=direct&via=website"
    }

    object Browser {
        const val HEADLESS: Boolean = false
        const val SLOW_MO: Double = (1 * 1000).toDouble()
        const val TIMEOUT: Double = 60000.toDouble()

        fun launchOptions(): BrowserType.LaunchOptions {
            return BrowserType.LaunchOptions()
                .setHeadless(HEADLESS)
                .setSlowMo(SLOW_MO)
        }
    }

    object Viewports {
        // Mobile devices
        val MOBILE_PORTRAIT = Viewport(390, 844, "iPhone 13", true)
        val MOBILE_LANDSCAPE = Viewport(844, 390, "iPhone 13 Landscape", true)
        val MOBILE_SMALL = Viewport(320, 568, "iPhone SE", true)
        val ANDROID = Viewport(412, 915, "Pixel 5", true)

        // Tablets
        val TABLET_PORTRAIT = Viewport(768, 1024, "iPad", true)
        val TABLET_LANDSCAPE = Viewport(1024, 768, "iPad Landscape", true)
        val TABLET_PRO = Viewport(1024, 1366, "iPad Pro", true)

        // Desktop
        val DESKTOP_HD = Viewport(1280, 720, "Desktop HD", false)
        val DESKTOP_FHD = Viewport(1920, 1080, "Desktop Full HD", false)
        val DESKTOP_4K = Viewport(3840, 2160, "Desktop 4K", false)
        val LAPTOP = Viewport(1366, 768, "Laptop", false)

        // All viewports for comprehensive testing
        val ALL_MOBILE = listOf(MOBILE_PORTRAIT, MOBILE_LANDSCAPE, ANDROID)
        val ALL_TABLET = listOf(TABLET_PORTRAIT, TABLET_LANDSCAPE)
        val ALL_DESKTOP = listOf(DESKTOP_HD, DESKTOP_FHD, LAPTOP)
        val ALL = ALL_MOBILE + ALL_TABLET + ALL_DESKTOP
    }

    object TestUsers {
        val NEW_USER = TestUser(
            mobileNumber = "726408324",
            otp = "678901"
        )

        val EXISTING_USER = TestUser(
            mobileNumber = "7373791414",
            otp = "678901"
        )
    }

    object Artifacts {
        const val SCREENSHOT_DIR = "build/screenshots"
        const val VIDEO_DIR = "build/videos"
        const val RECORD_VIDEO = true
    }

    object Timeouts {
        const val NAVIGATION_TIMEOUT = 60000L
        const val ELEMENT_TIMEOUT = 10000L
    }
}

data class Viewport(
    val width: Int,
    val height: Int,
    val name: String,
    val isMobile: Boolean,
    val hasTouch: Boolean = isMobile,
    val deviceScaleFactor: Double = if (isMobile) 2.0 else 1.0
)

data class TestUser(
    val mobileNumber: String,
    val otp: String,
)
