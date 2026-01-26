package config

import com.microsoft.playwright.BrowserType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json


object TestConfig {

    var ACCESS_TOKEN = ""
    const val CLIENT_ID = "6wkFaU5pxWn4R3C87Z6ceNQgzLJvD2OVKoXB"


    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = true
        encodeDefaults = true
    }

    object Urls {
        val BASE_URL: String = "https://app.deepholistics.com"
        val DIAGNOSTICS_URL: String = "$BASE_URL/diagnostics"
        val LAB_TEST_API_URL: String = "https://api.stg.dh.deepholistics.com/v4/human-token/lab-test"
        val HOME_PAGE_URL: String = "$BASE_URL/home"
        val PROFILE_PAGE_URL: String = "$BASE_URL/profile"
        val SYMPTOMS_PAGE_URL: String = "$BASE_URL/insights"

        val LOGIN_PATH = "/login"
        val HOME_PATH = "/home"
        val DIAGNOSTICS_PATH = "$BASE_URL/diagnostics"

        // val HOME_PAGE_URL = "$BASE_URL/home"
        val PROFILE_URL = "$BASE_URL/profile"
    }


    object APIs {
        val BASE_URL: String = "https://api.dh.deepholistics.com"

        val API_ADDRESS = "$BASE_URL/v4/human-token/market-place/address"
        val API_UPDATE_PROFILE = "$BASE_URL/v4/human-token/lead/update-profile"
        val API_TONE_PREFERENCE = "$BASE_URL/v4/human-token/preference"
        val API_PREFERENCE = "$BASE_URL/v4/human-token/preference?fields=communication_preference"
        val API_PREFERENCE_UPDATE = "$BASE_URL/v4/human-token/preference"
        val API_ACCOUNT_INFORMATION = "$BASE_URL/v4/human-token/pii-data"
        val API_VERIFY_OTP = "$BASE_URL/v4/human-token/lead/verify-otp"
        val API_SYMPTOMS_LIST = "$BASE_URL/v4/human-token/health-data/symptom/list"
        val API_MARKETPLACE = "$BASE_URL/v4/human-token/market-place/home"
    }



    object Browser {
        val HEADLESS: Boolean = false
        val SLOW_MO: Double = (1 * 1000).toDouble()
        val TIMEOUT: Double = 60000.toDouble()

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
            mobileNumber = "9952538864",
            otp = "678901"
        )


        /*    val EXISTING_USER = TestUser(
                mobileNumber = "8870208411",
                otp = "678901"
            )*/
    }

    object Artifacts {
        val SCREENSHOT_DIR = "build/screenshots"
        val VIDEO_DIR = "build/videos"
        val TRACE_DIR = "build/traces"
        val SCREENSHOT_ON_FAILURE = true
        val RECORD_VIDEO = true
    }

    object Timeouts {
        const val DEFAULT_TIMEOUT = 30000L
        const val NAVIGATION_TIMEOUT = 60000L
        const val ELEMENT_TIMEOUT = 10000L
        const val ANIMATION_TIMEOUT = 500L
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
