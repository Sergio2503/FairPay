package com.fairpay.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JwtService(
    private val secret: String,
    private val issuer: String,
    private val audience: String
) {

    fun generateToken(userId: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000)) // 24h
            .sign(Algorithm.HMAC256(secret))
}
