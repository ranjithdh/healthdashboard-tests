package utils

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.WaitForSelectorState

fun Locator.waitUntilHidden(timeoutMs: Double = 5000.0) {
    this.waitFor(
        Locator.WaitForOptions()
            .setState(WaitForSelectorState.HIDDEN)
            .setTimeout(timeoutMs)
    )
}

fun Locator.waitUntilDetached(timeoutMs: Double = 5000.0) {
    this.waitFor(
        Locator.WaitForOptions()
            .setState(WaitForSelectorState.DETACHED)
            .setTimeout(timeoutMs)
    )
}
