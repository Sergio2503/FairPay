package com.fairpay.database.tables

import org.jetbrains.exposed.sql.Table

object ExpenseParticipantsTable : Table("expense_participants") {

    val id = varchar("id", 8)

    val expense = varchar("expense_id", 8)
        .references(ExpensesTable.id)

    val user = varchar("user_id", 8)
        .references(UsersTable.id)

    val amount = decimal("amount", 10, 2)

    override val primaryKey = PrimaryKey(id)
}