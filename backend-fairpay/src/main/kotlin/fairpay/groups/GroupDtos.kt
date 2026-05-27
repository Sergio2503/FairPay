package com.fairpay.groups

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String
)

@Serializable
data class JoinGroupRequest(
    val groupId: String
)

@Serializable
data class GroupResponse(
    val id: String,
    val name: String,
    val createdBy: String,
    val createdAt: String,
    val memberCount: Int
)

@Serializable
data class GroupMemberResponse(
    val userId: String,
    val name: String,
    val email: String,
    val joinedAt: String
)

@Serializable
data class CreateGroupResponse(
    val groupId: String
)