package com.fairpay.auth

import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.principal

fun ApplicationCall.userId(): String {
    val principal = principal<JWTPrincipal>()
        ?: throw IllegalStateException("Usuario no autenticado")

    return principal.payload
        .getClaim("userId")
        .asString()
}