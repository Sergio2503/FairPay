package com.fairpay.expenses

import kotlinx.serialization.Serializable

@Serializable
data class CreateExpenseRequest(
    val groupId: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val participants: List<String>
)

@Serializable
data class ExpenseResponse(
    val id: String,
    val groupId: String,
    val paidBy: String,
    val paidByName: String,
    val title: String,
    val amount: Double,
    val createdAt: String
)