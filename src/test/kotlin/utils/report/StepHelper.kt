package utils.report

import io.qameta.allure.Allure

object StepHelper {

    fun step(name: String) {
        Allure.step(name)
    }

    const val CLEAR_MOBILE_NUMBER = "Clear Mobile Number"
}