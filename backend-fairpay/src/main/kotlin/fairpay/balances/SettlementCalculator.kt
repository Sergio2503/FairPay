package com.fairpay.balances

import kotlin.math.abs

fun calculateSettlements(
    balances: Map<String, Double>
): List<Settlement> {

    val creditors = balances
        .filterValues { it > 0.0 }
        .map { it.key to it.value }
        .toMutableList()

    val debtors = balances
        .filterValues { it < 0.0 }
        .map { it.key to abs(it.value) }
        .toMutableList()

    val settlements = mutableListOf<Settlement>()

    var i = 0
    var j = 0

    while (i < debtors.size && j < creditors.size) {
        val (debtorId, debt) = debtors[i]
        val (creditorId, credit) = creditors[j]

        val amount = minOf(debt, credit)

        settlements.add(
            Settlement(
                fromUserId = debtorId,
                toUserId = creditorId,
                amount = amount
            )
        )

        val remainingDebt = debt - amount
        val remainingCredit = credit - amount

        debtors[i] = debtorId to remainingDebt
        creditors[j] = creditorId to remainingCredit

        if (remainingDebt <= 0.00001) i++
        if (remainingCredit <= 0.00001) j++
    }

    return settlements
}