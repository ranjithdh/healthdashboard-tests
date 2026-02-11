package mobileView.actionPlan.utils

import kotlin.math.roundToInt

object ActionPlanUtils {

    fun ninetyPercent(value: Double): Int {
        return (value * 0.9).roundToInt()
    }

}