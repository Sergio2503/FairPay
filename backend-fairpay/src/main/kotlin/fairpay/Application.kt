package com.fairpay

import com.fairpay.auth.JwtService
import com.fairpay.config.configureHttp
import com.fairpay.config.configureRouting
import com.fairpay.config.configureSecurity
import com.fairpay.config.configureSerialization
import com.fairpay.database.DatabaseFactory
import io.ktor.server.application.Application

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    DatabaseFactory.init(environment)

    configureSerialization()
    configureSecurity()
    configureHttp()

    val jwtService = JwtService(
        secret = environment.config.property("jwt.secret").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString()
    )

    configureRouting(jwtService)
}