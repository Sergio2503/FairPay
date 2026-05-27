package com.fairpay.balances

import com.fairpay.database.tables.ExpenseParticipantsTable
import com.fairpay.database.tables.ExpensesTable
import com.fairpay.database.tables.GroupMembersTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.RoundingMode
import org.jetbrains.exposed.sql.selectAll

@kotlinx.serialization.Serializable
data class BalanceResponse(
    val userId: String,
    val balance: Double
)

fun calculateGroupBalances(groupId: String): List<BalanceResponse> = transaction {

    val ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
    val balances = linkedMapOf<String, BigDecimal>()

    // 1. Inicializar usuarios del grupo
    GroupMembersTable
        .select { GroupMembersTable.group eq groupId }
        .forEach { row ->
            val userId = row[GroupMembersTable.user]
            balances[userId] = ZERO
        }

    // 2. Sumar lo que cada uno pagó
    ExpensesTable
        .select { ExpensesTable.group eq groupId }
        .forEach { row ->
            val paidBy = row[ExpensesTable.paidBy]
            val amount = row[ExpensesTable.amount].setScale(2, RoundingMode.HALF_UP)

            balances[paidBy] = balances
                .getOrDefault(paidBy, ZERO)
                .add(amount)
        }

    // 3. Restar lo que cada uno debe
    ExpenseParticipantsTable
        .selectAll()
        .forEach { row: org.jetbrains.exposed.sql.ResultRow ->

            val expenseId = row[ExpenseParticipantsTable.expense]

            // comprobar si el gasto pertenece al grupo
            val expense = ExpensesTable
                .select { ExpensesTable.id eq expenseId }
                .singleOrNull() ?: return@forEach

            if (expense[ExpensesTable.group] != groupId) return@forEach

            val userId = row[ExpenseParticipantsTable.user]

            val share = row[ExpenseParticipantsTable.amount]
                .setScale(2, RoundingMode.HALF_UP)

            balances[userId] = balances
                .getOrDefault(userId, ZERO)
                .subtract(share)
        }

    // 4. Resultado final
    balances.map { (userId, balance) ->
        BalanceResponse(
            userId = userId,
            balance = balance.setScale(2, RoundingMode.HALF_UP).toDouble()
        )
    }
}