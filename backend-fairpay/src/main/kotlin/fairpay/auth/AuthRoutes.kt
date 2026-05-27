package com.fairpay.auth

import com.fairpay.database.tables.UsersTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.authRoutes(jwtService: JwtService) {

    route("/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()

            val existingEmail = transaction {
                UsersTable
                    .select { UsersTable.email eq request.email }
                    .limit(1)
                    .firstOrNull()
            }

            if (existingEmail != null) {
                call.respond(HttpStatusCode.Conflict, "El email ya está registrado")
                return@post
            }

            val existingUsername = transaction {
                UsersTable
                    .select { UsersTable.username eq request.name }
                    .limit(1)
                    .firstOrNull()
            }

            if (existingUsername != null) {
                call.respond(HttpStatusCode.Conflict, "El nombre de usuario ya está registrado")
                return@post
            }

            val userId = newUniqueUserId()
            val hashed = PasswordHasher.hash(request.password)

            transaction {
                UsersTable.insert { row ->
                    row[UsersTable.id] = userId
                    row[UsersTable.username] = request.name
                    row[UsersTable.email] = request.email
                    row[UsersTable.passwordHash] = hashed
                }
            }

            val token = jwtService.generateToken(userId)
            call.respond(HttpStatusCode.Created, AuthResponse(token))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val user = transaction {
                UsersTable
                    .select { UsersTable.email eq request.email }
                    .limit(1)
                    .firstOrNull()
            }

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas")
                return@post
            }

            val storedHash = user[UsersTable.passwordHash]
            val valid = PasswordHasher.verify(request.password, storedHash)

            if (!valid) {
                call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas")
                return@post
            }

            val userId = user[UsersTable.id]
            val token = jwtService.generateToken(userId)
            call.respond(AuthResponse(token))
        }

        authenticate("auth-jwt") {

            get("/me") {

                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, "No autorizado")

                val userId = principal.getClaim("userId", String::class)
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, "Token inválido")

                val user = transaction {
                    UsersTable
                        .select { UsersTable.id eq userId }
                        .map {
                            mapOf(
                                "id" to it[UsersTable.id],
                                "username" to it[UsersTable.username],
                                "email" to it[UsersTable.email],
                                "createdAt" to it[UsersTable.createdAt].toString()
                            )
                        }
                        .singleOrNull()
                }

                call.respond(user ?: HttpStatusCode.NotFound)
            }
        }
    }
}

private fun newUniqueUserId(): String {
    while (true) {
        val candidate = com.fairpay.util.IdGenerator.newId8()

        val exists = transaction {
            UsersTable
                .select { UsersTable.id eq candidate }
                .limit(1)
                .firstOrNull() != null
        }

        if (!exists) return candidate
    }
}