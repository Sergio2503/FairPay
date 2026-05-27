package com.fairpay.groups

import com.fairpay.database.tables.GroupMembersTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun groupMemberIds(groupId: String): Set<String> = transaction {
    GroupMembersTable
        .select { GroupMembersTable.group eq groupId }
        .map { it[GroupMembersTable.user] }
        .toSet()
}