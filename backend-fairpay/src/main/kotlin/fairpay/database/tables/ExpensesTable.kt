package com.fairpay.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object ExpensesTable : Table("expenses") {

    val id = varchar("id", 8)

    val group = varchar("group_id", 8)
        .references(GroupsTable.id)

    val paidBy = varchar("paid_by", 8)
        .references(UsersTable.id)

    val title = varchar("title", 255)

    val amount = decimal("amount", 10, 2)

    val createdAt = timestamp("created_at")
        .defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}