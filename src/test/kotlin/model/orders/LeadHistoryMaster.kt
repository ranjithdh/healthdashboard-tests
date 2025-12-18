package model.orders

data class LeadHistoryMaster(
    val appointOn: List<AppointOn>,
    val assignTspOn: List<AssignTspOn>,
    val bookedOn: List<BookedOn>,
    val deliverdOn: List<Any>,
    val reappointOn: List<Any>,
    val rejectedOn: Any,
    val reportedOn: List<Any>,
    val servicedOn: List<Any>
)