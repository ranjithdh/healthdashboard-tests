package model.orders

data class OrderMaster(
    val address: String,
    val appointmentId: Any,
    val bookingThrough: String,
    val cancelRemarks: String,
    val cmlt: String,
    val ctlOrderNos: Any,
    val email: String,
    val feedback: String,
    val ids: String,
    val incentive: String,
    val mobile: String,
    val names: String,
    val orderNo: String,
    val parentOrderNo: String,
    val patinetId: Any,
    val payType: String,
    val pincode: String,
    val products: String,
    val rate: String,
    val refByDRName: String,
    val remarks: String,
    val serviceType: String,
    val status: String,
    val tsp: String,
    val ulc: String
)