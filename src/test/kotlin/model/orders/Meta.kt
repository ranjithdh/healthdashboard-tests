package model.orders

data class Meta(
    val benMaster: List<BenMaster>,
    val collectionCenters: Any,
    val leadHistoryMaster: List<LeadHistoryMaster>,
    val mergedOrderNos: Any,
    val orderMaster: List<OrderMaster>,
    val phleboDetail: PhleboDetail,
    val qr: Any,
    val respId: String,
    val response: String,
    val tspMaster: List<TspMaster>
)