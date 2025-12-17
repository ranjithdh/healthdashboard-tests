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
│   │   ├── TestConfig.kt      # Configuration & viewports
│   │   └── BaseTest.kt        # Base test classes
│   ├── pages/
│   │   ├── BasePage.kt        # Base page object
│   │   ├── LoginPage.kt       # Login page object
│   │   └── DashboardPage.kt   # Dashboard page object
│   ├── dsl/
│   │   └── HealthDashboardDsl.kt  # Fluent DSL for tests
│   ├── tests/
│   │   ├── LoginTest.kt           # Desktop login tests
│   │   ├── LoginMobileTest.kt     # Mobile login tests
│   │   ├── LoginTabletTest.kt     # Tablet login tests
│   │   ├── ResponsiveTest.kt      # Cross-viewport tests
│   │   └── DslExampleTest.kt      # DSL example tests
│   └── utils/
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
# or
./gradlew test -Dkotest.tags=Mobile
```

### Desktop Tests Only
```bash
./gradlew desktopTests
# or
./gradlew test -Dkotest.tags=Desktop
```

### Tablet Tests Only
```bash
./gradlew test -Dkotest.tags=Tablet
```

### Specific Test Class
```bash
./gradlew test --tests "LoginTest"
./gradlew test --tests "LoginMobileTest"
```

### With Browser UI (Non-Headless)
```bash
HEADLESS=false ./gradlew test
```

### With Slow Motion (Debugging)
```bash
SLOW_MO=100 ./gradlew test
```

## 📱 Viewport Configurations

| Name | Dimensions | Type |
|------|------------|------|
| iPhone 13 | 390x844 | Mobile |
| iPhone SE | 320x568 | Mobile Small |
| Pixel 5 | 412x915 | Android |
| iPad | 768x1024 | Tablet |
| iPad Pro | 1024x1366 | Tablet |
| Laptop | 1366x768 | Desktop |
| Desktop HD | 1280x720 | Desktop |
| Desktop FHD | 1920x1080 | Desktop |

## ✍️ Writing Tests

### Using Page Objects (Traditional)
```kotlin
class MyTest : BaseTest({
    
    test("user can login") {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val dashboard = loginPage.loginAs(TestConfig.TestUsers.VALID_USER)
        
        dashboard.isDashboardLoaded().shouldBeTrue()
    }
})
```

### Using Fluent DSL (Recommended)
```kotlin
class MyTest : BaseTest({
    
    test("user can login on mobile") {
        page.healthDashboard()
            .onMobile()
            .goToLogin()
            .shouldBeOnLogin()
            .loginAsValidUser()
            .shouldBeOnDashboard()
            .takeScreenshot("login-success")
    }
})
```

### Mobile-Specific Tests
```kotlin
class MyMobileTest : MobileTest({
    
    test("shows mobile navigation") {
        val loginPage = LoginPage(page).navigate() as LoginPage
        loginPage.isMobileMenuVisible().shouldBeTrue()
    }
})
```

### Data-Driven Tests (Multiple Viewports)
```kotlin
class ResponsiveTest : FunSpec({
    
    context("Login form displays on all viewports") {
        withData(TestConfig.Viewports.ALL) { viewport ->
            // Test runs for each viewport
            val page = createPageWithViewport(viewport)
            LoginPage(page).isLoginFormDisplayed().shouldBeTrue()
        }
    }
})
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

### Extending the DSL

In `HealthDashboardDsl.kt`:

```kotlin
fun myCustomAction(): HealthDashboardDsl {
    // Your action
    return this
}
```

## 📝 Best Practices

1. **Use data-testid attributes** in your React app for reliable selectors
2. **Keep tests independent** - each test should be able to run in isolation
3. **Use the DSL** for readable, maintainable tests
4. **Take screenshots** at key points for visual regression
5. **Tag tests appropriately** (Mobile, Desktop, Tablet) for selective runs

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
