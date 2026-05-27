package com.fairpay.config

import com.fairpay.auth.JwtService
import com.fairpay.auth.authRoutes
import com.fairpay.balances.balanceRoutes
import com.fairpay.balances.settlementRoutes
import com.fairpay.groups.groupRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting(jwtService: JwtService) {

    routing {
        get("/") {
            call.respondText("FairPay backend running")
        }

        authRoutes(jwtService)
        groupRoutes()
        balanceRoutes()
        settlementRoutes()
    }
}