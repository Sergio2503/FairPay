package com.fairpay.balances

import com.fairpay.auth.userId
import com.fairpay.groups.groupMemberIds
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.settlementRoutes() {

    authenticate("auth-jwt") {

        route("/balances") {

            // Obtener balances del grupo
            get("/{groupId}") {

                val groupId = call.parameters["groupId"]
                    ?: return@get call.respondText(
                        "Missing groupId",
                        status = HttpStatusCode.BadRequest
                    )

                val currentUserId = call.userId()
                val members = groupMemberIds(groupId)

                if (currentUserId !in members) {
                    return@get call.respond(
                        HttpStatusCode.Forbidden,
                        "No perteneces a este grupo"
                    )
                }

                val balances = calculateGroupBalances(groupId)

                call.respond(balances)
            }
        }
    }
}