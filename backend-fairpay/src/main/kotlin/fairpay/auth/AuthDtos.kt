package com.fairpay.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String
)

@Serializable
data class MeResponse(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: String
)