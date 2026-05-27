package com.fairpay.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {

    val id = char("id", 8)

    val username = varchar("username", 100)

    val email = varchar("email", 150).uniqueIndex()

    val passwordHash = text("password_hash")

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}