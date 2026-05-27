package com.fairpay.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object GroupsTable : Table("groups") {

    val id = char("id", 8)

    val name = varchar("name", 150)

    val createdBy = char("created_by", 8)
        .references(UsersTable.id, onDelete = ReferenceOption.CASCADE)

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}