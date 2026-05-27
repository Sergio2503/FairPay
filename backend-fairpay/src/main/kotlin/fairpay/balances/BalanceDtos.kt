package com.fairpay.balances

import kotlinx.serialization.Serializable

@Serializable
data class UserBalanceResponse(
    val userId: String,
    val balance: Double
)
