package model

import kotlinx.serialization.Serializable

@Serializable
data class WalletResponse(
    val status: String,
    val message: String,
    val data: WalletData? = null
)

@Serializable
data class WalletData(
    val user_wallet: UserWallet
)

@Serializable
data class UserWallet(
    val id: String,
    val user_id: String,
    val current_balance: String
)
