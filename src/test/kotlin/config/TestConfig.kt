package config

import com.microsoft.playwright.BrowserType


object TestConfig {
    val isStaging: Boolean = true
    var ACCESS_TOKEN = ""
    const val CLIENT_ID = "qXsGPcHJkb9MTwD5fNFpzRrngjtvy4dW"

    object Urls {
        private const val STG_BASE = "https://app.stg.deepholistics.com/"
        private const val PROD_BASE = "https://app.deepholistics.com/"

        val BASE_URL: String = if (isStaging) STG_BASE else PROD_BASE

        // const val BASE_URL: String = "https://app.stg.deepholistics.com/"

        val DIAGNOSTICS_PATH = "${BASE_URL}diagnostics"

        val SERVICES_URL = "${BASE_URL}services"


        val LOGIN_URL = "${BASE_URL}login?utm_source=direct"
        val DIAGNOSTICS_URL: String = "${BASE_URL}diagnostics"

        val PROFILE_PAGE_URL: String = "${BASE_URL}profile"
        val HEALTH_DATA_URL = "${BASE_URL}health-data"

        val HOME_PAGE_URL = "${BASE_URL}home"
        val WEBSITE_HOME_PAGE_URL =
            if (isStaging) "https://app.stg.deepholistics.com" else "https://app.deepholistics.com"
        val PROFILE_URL = "${BASE_URL}profile"


        val WEBSITE_BASE_URL: String = "https://www.deepholistics.com/"
        val HOW_IT_WORKS: String = "${WEBSITE_BASE_URL}how-it-works"
        val WHAT_WE_TEST: String = "${WEBSITE_BASE_URL}what-we-test"
        val OUR_WHY: String = "${WEBSITE_BASE_URL}our-why"
        val FAQ: String = "${WEBSITE_BASE_URL}faq"
        val ALL_TEST: String = "${WEBSITE_BASE_URL}all-tests"
        val ALLERGY_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/allergies"
        val GUT_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/gut"
        val STRESS_CORTISOL_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/stress-and-cortisol"
        val GENE_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/gene"
        val OMEGA_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/omega-profile"
        val TOXIC_METALS_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/toxic-metals"
        val THYROID_HEALTH_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/thyroid-health"
        val WOMEN_HEALTH_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/womens-health"
        val ESSENTIAL_AND_NUTRIENTS_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/essential-nutrients"
        val ADVANCED_THYROID_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/advanced-thyroid"
        val LIVER_HEALTH_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/liver-health"
        val AUTO_IMMUNE_PANEL_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/autoimmune"
        val HEART_HEALTH_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/heart-health"
        val WOMEN_FERTILITY_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/womens-fertility"
        val BLOOD_HEALTH_DETAIL: String = "${WEBSITE_BASE_URL}add-on-test/blood-health"


        val SIGNUP_VIA_WEBSITE = "https://app.deepholistics.com/login?mode=signup&utm_source=direct&via=website"
        val LOGIN_VIA_WEBSITE = "https://app.deepholistics.com/login?utm_source=direct&via=website"
        val SYMPTOMS_PAGE_URL: String = "${BASE_URL}insights"
    }

    object APIs {
        private const val STG_API = "https://api.stg.dh.deepholistics.com"
        private const val PROD_API = "https://api.dh.deepholistics.com"

        val BASE_URL: String = if (isStaging) STG_API else PROD_API

        //  const val BASE_URL: String = "https://api.stg.dh.deepholistics.com"
        val SERVICE_SEARCH_API_URL = "https://api.stg.dh.deepholistics.com/v4/human-token/market-place/products"
        val LAB_TEST_API_URL: String = "https://api.stg.dh.deepholistics.com/v4/human-token/lab-test"
        val API_ADDRESS = "$BASE_URL/v4/human-token/market-place/address"
        val API_UPDATE_PROFILE = "$BASE_URL/v4/human-token/lead/update-profile"
        val API_TONE_PREFERENCE = "$BASE_URL/v4/human-token/preference"
        val API_PREFERENCE = "$BASE_URL/v4/human-token/preference?fields=communication_preference"
        val API_PREFERENCE_UPDATE = "$BASE_URL/v4/human-token/preference"
        val API_ACCOUNT_INFORMATION = "$BASE_URL/v4/human-token/pii-data"
        val API_VERIFY_OTP = "$BASE_URL/v4/human-token/lead/verify-otp"
        val API_SLOTS_AVAILABILITY = "$BASE_URL/v3/diagnostics/slots-availability"
        val API_SYMPTOMS_LIST = "$BASE_URL/v4/human-token/health-data/symptom/list"
    }


       object Browser {
            const val SLOW_MO: Double = (1 * 1000).toDouble()
            const val TIMEOUT: Double = 60000.toDouble()

            fun launchOptions(): BrowserType.LaunchOptions {
                val isHeadless = System.getenv("HEADLESS")?.toBoolean()
                    ?: System.getProperty("headless")?.toBoolean()
                    ?: false   //TODO default safe for CI is true

                return BrowserType.LaunchOptions()
                    .setHeadless(isHeadless)
                    .setSlowMo(if (isHeadless) 0.0 else SLOW_MO)
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
            mobileNumber = System.getenv("TEST_USER_MOBILE") ?: "573583618",
            otp = System.getenv("TEST_USER_OTP") ?: "",
            firstName = "ranjith",
            email = "ranjithkumar.m@mysmitch.com",
            gender = "Male",
            height = "170",
            weight = "60",
            month = "2",
            year = "1998",
            day = "12",
            flatHouseNo = "Flat 101",
            address = "456 Main Road",
            city = "Coimbatore",
            state = "TamilNadu",
            pinCode = "641005",
            country = "Poland"
        )


        /*val EXISTING_USER = TestUser(
                    mobileNumber = System.getenv("EXISTING_USER_MOBILE") ?: "9677004512",
                    otp = System.getenv("EXISTING_USER_OTP") ?: "678901",
                    country = "India"
                )*/

        val EXISTING_USER = TestUser(
              mobileNumber = "7373791414",
              otp = "678901",
              country = "India"
          )

      /*  val EXISTING_USER = TestUser(
            mobileNumber = "9159439327",
            otp = "678901",
            country = "India"
        )*/
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

    object Coupons {
        const val VALID_COUPON = "D261C0"
        const val INVALID_COUPON = "INVALID123"
        const val DISCOUNT_AMOUNT = 1000f
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
    val firstName: String = "Test",
    val email: String = "test@test.com",
    val gender: String = "Male",
    val height: String = "170",
    val weight: String = "60",
    val month: String = "2",
    val year: String = "1998",
    val day: String = "12",
    val flatHouseNo: String = "Flat 101",
    val address: String = "456 Main Road",
    val city: String = "Delhi",
    val state: String = "Delhi",
    val pinCode: String = "110001",
    val country: String = "India"
)
