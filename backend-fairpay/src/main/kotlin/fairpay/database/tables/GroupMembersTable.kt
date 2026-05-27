package com.fairpay.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object GroupMembersTable : Table("group_members") {

    val group = char("group_id", 8)
        .references(GroupsTable.id, onDelete = ReferenceOption.CASCADE)

    val user = char("user_id", 8)
        .references(UsersTable.id, onDelete = ReferenceOption.CASCADE)

    val joinedAt = timestamp("joined_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(group, user)
}