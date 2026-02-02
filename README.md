# HealthDashboard E2E Test Automation

Playwright + Kotlin test automation framework for the HealthDashboard application, supporting mobile, tablet, and desktop viewports.

## 🚀 Quick Start

### Prerequisites

- JDK 17+
- Node.js 18+ (for Playwright browser installation)
- Gradle 8+ (or use wrapper)

### Setup

```bash
# Clone the repository
git clone <your-repo-url>
cd healthdashboard-tests

# Install Playwright browsers (using Gradle task)
./gradlew installPlaywright

# Run all tests
./gradlew test
```

## 📁 Project Structure

```
healthdashboard-tests/
├── src/test/kotlin/
│   ├── config/
│   │   ├── BasePage.kt        # Base page object
│   │   └── TestConfig.kt      # Configuration & viewports
│   ├── onboard/
│   │   ├── page/              # Login-related POMs
│   │   └── test/              # Login & Signup tests (fullflow, signup, etc.)
│   ├── mobileView/
│   │   ├── home/              # Mobile home page & tests
│   │   └── orders/            # Mobile orders page & tests
│   ├── model/                 # Data models for API/JSON parsing
│   ├── profile/               # Profile-related POMs
│   ├── webView/                # Web diagnostic tests
│   └── utils/                 # Utilities (JSON, Logger, DateHelper, etc.)
├── build.gradle.kts
├── settings.gradle.kts
└── .github/workflows/
    └── e2e-tests.yml          # CI/CD pipeline
```

## 🧪 Running Tests

### All Tests
```bash
./gradlew test
```

### Mobile Tests Only
```bash
./gradlew mobileTests
```

### Desktop Tests Only
```bash
./gradlew desktopTests
```

### Specific Test Class
```bash
./gradlew test --tests "onboard.test.fullflow.SignUpFlowTest"
./gradlew test --tests "onboard.test.fullflow.LoginFlowTest"
```

### With Browser UI (Non-Headless)
```bash
HEADLESS=false ./gradlew test
```

## 📱 Viewport Configurations

| Name | Dimensions | Type |
|------|------------|------|
| Android | 390x844 | Mobile |
| Laptop | 1366x768 | Desktop |

## ✍️ Writing Tests

### Using Page Objects with Method Chaining
```kotlin
@Test
fun `should complete full signup flow`() {
    val loginPage = LoginPage(page).navigate() as LoginPage

    val homePage = loginPage
        .clickSignUp()
        .enterMobileAndContinue("XXXXXXXXX")
        .enterOtpAndContinueToAccountCreation("XXXX")
        .fillAndContinue("First", "Last", "test@test.com")
        // ... continue through flow ...
        .waitForMobileHomePageConfirmation()

    assertTrue(homePage.isSavedFullSlotMatchingApi())
}
```

### Mobile-Specific Tests
```kotlin
@Test
fun `login and check blood test status`() {
    val user = TestConfig.TestUsers.NEW_USER
    val loginPage = LoginPage(page).navigate() as LoginPage
    
    val homePage = loginPage
        .enterMobileAndContinue(user.mobileNumber)
        .enterOtpAndContinueToMobileHomePage(user.otp)

    assertTrue(homePage.isBloodTestCardVisible())
}
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `BASE_URL` | Application URL | `https://app.stg.deepholistics.com` |
| `HEADLESS` | Run headless | `true` |
| `SLOW_MO` | Slow down actions (ms) | `0` |
| `TEST_USER_EMAIL` | Test user email | - |
| `TEST_USER_PASSWORD` | Test user password | - |
| `RECORD_VIDEO` | Record test videos | `false` |

### Test User Setup

Create a `.env` file or set environment variables:

```bash
export TEST_USER_EMAIL=test@deepholistics.com
export TEST_USER_PASSWORD=YourTestPassword123
```

## 📊 Reports

### Kotest HTML Report
```bash
./gradlew test
# View: build/reports/tests/test/index.html
```

### Allure Report
```bash
./gradlew allureReport
./gradlew allureServe  # Opens in browser
# View: build/reports/allure-report/index.html
```

### Screenshots
Screenshots are saved to `build/screenshots/` on test failure or when explicitly captured.

## 🔄 CI/CD Integration

The project includes GitHub Actions workflows:

- **On Push/PR**: Runs all tests
- **Daily**: Scheduled regression tests
- **Manual**: Run specific test tags

### Required Secrets

Add these to your GitHub repository secrets:
- `TEST_USER_EMAIL`
- `TEST_USER_PASSWORD`

## 🛠️ Customization

### Adding New Page Objects

1. Create a new class extending `BasePage`:

```kotlin
class MyNewPage(page: Page) : BasePage(page) {
    override val pageUrl = "/my-page"
    override val pageLoadedSelector = "[data-testid='my-page']"
    
    // Add selectors and methods
}
```

### Adding New Viewports

In `TestConfig.kt`:

```kotlin
val MY_CUSTOM_VIEWPORT = Viewport(
    width = 800,
    height = 600,
    name = "Custom Device",
    isMobile = false
)
```

## 📝 Best Practices

1. **Use data-testids** for reliable selectors.
2. **Method Chaining**: Use the page object method chaining for readable, maintainable tests.
3. **Independent Tests**: Each test should be able to run in isolation.
4. **Visual Verification**: Take screenshots at key points.
5. **Tagging**: Tag tests appropriately for selective execution.

## 🐛 Troubleshooting

### Browser not found
```bash
# Install browsers using Gradle task
./gradlew installPlaywright

# If that fails, browsers will be downloaded automatically on first test run
# You can also try clearing Playwright cache:
rm -rf ~/.cache/ms-playwright
./gradlew installPlaywright
```

### Tests timing out
Increase timeout in `TestConfig.kt`:
```kotlin
const val DEFAULT_TIMEOUT = 60000L  // 60 seconds
```

### Element not found
- Check if selectors match your actual HTML
- Update selectors in page objects
- Use Playwright Inspector: `PWDEBUG=1 ./gradlew test`

## 📚 Resources

- [Playwright Java Docs](https://playwright.dev/java/)
- [Kotest Framework](https://kotest.io/)
- [Allure Reporting](https://docs.qameta.io/allure/)
