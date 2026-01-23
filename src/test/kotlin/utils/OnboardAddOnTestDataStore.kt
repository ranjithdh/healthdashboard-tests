package utils

import model.addontest.OnboardDiagnosticProductList

object OnboardAddOnTestDataStore {
    private val addOnTests = ThreadLocal.withInitial { OnboardDiagnosticProductList() }

    fun get(): OnboardDiagnosticProductList {
        return addOnTests.get()
    }

    fun update(fn: OnboardDiagnosticProductList.() -> Unit) {
        addOnTests.get().fn()
    }

    fun clear() {
        addOnTests.remove()
    }
}
