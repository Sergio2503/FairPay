package com.fairpay.balances

import kotlinx.serialization.Serializable

@Serializable
data class Settlement(
    val fromUserId: String,
    val toUserId: String,
    val amount: Double
)
