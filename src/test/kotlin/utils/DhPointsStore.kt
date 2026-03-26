package utils

import utils.logger.logger
import java.io.File
import java.util.Properties

/**
 * DhPointsStore persists DH Points test data to a local file so it
 * survives across separate JUnit @Test invocations (which run in separate
 * threads / classloaders).
 *
 * Usage:
 *   DhPointsStore.save(totalAmount = "8999", discountAmount = "1000", couponCode = "IMPL123")
 *   DhPointsStore.totalAmount   // -> "8999"
 */
object DhPointsStore {

    private val storeFile = File("build/dh_points_store.properties")

    // Always read straight from file so we never return a stale in-memory value
    val totalAmount: String
        get() = load().getProperty("totalAmount", "")

    val discountAmount: String
        get() = load().getProperty("discountAmount", "")

    val couponCode: String
        get() = load().getProperty("couponCode", "")

    fun save(
        totalAmount: String = this.totalAmount,
        discountAmount: String = this.discountAmount,
        couponCode: String = this.couponCode
    ) {
        val props = Properties()
        props["totalAmount"] = totalAmount
        props["discountAmount"] = discountAmount
        props["couponCode"] = couponCode

        storeFile.parentFile?.mkdirs()
        storeFile.outputStream().use { props.store(it, "DH Points test data") }

        logger.info { "[DhPointsStore] Saved -> totalAmount=$totalAmount, discountAmount=$discountAmount, couponCode=$couponCode" }
    }

    fun clear() {
        if (storeFile.exists()) storeFile.delete()
        logger.info { "[DhPointsStore] Cleared." }
    }

    private fun load(): Properties {
        val props = Properties()
        if (storeFile.exists()) {
            storeFile.inputStream().use { props.load(it) }
        }
        return props
    }
}
