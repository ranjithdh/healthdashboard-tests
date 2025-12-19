package model.signup

import java.time.LocalDateTime

data class SignupData(
    var mobileNumber: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var month: String? = null,
    var year: String? = null,
    var day: String? = null,
    var gender: String? = null,
    var height: String? = null,
    var weight: String? = null,
    var flatHouseNoOrBuilding: String? = null,
    var address: String? = null,
    var city: String? = null,
    var state: String? = null,
    var pinCode: String? = null,
    var fastingSlot: String? = null,
    var postMealSlot: String? = null,
    var slotDate: LocalDateTime? = null,
)
