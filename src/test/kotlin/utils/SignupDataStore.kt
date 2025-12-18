package utils

import model.signup.SignupData

object SignupDataStore {
    private val signupDataThreadLocal = ThreadLocal.withInitial { SignupData() }

    fun get(): SignupData {
        return signupDataThreadLocal.get()
    }

    fun update(fn: SignupData.() -> Unit) {
        signupDataThreadLocal.get().fn()
    }

    fun clear() {
        signupDataThreadLocal.remove()
    }
}
