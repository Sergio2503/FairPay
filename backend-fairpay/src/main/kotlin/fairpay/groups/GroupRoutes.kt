package com.fairpay.groups

import com.fairpay.auth.userId
import com.fairpay.database.tables.*
import com.fairpay.expenses.CreateExpenseRequest
import com.fairpay.util.IdGenerator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.RoundingMode
import com.fairpay.database.tables.ExpensesTable
import com.fairpay.balances.calculateGroupBalances
import com.fairpay.expenses.ExpenseResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import io.ktor.server.routing.delete

fun Route.groupRoutes() {

    authenticate("auth-jwt") {

        route("/groups") {

            // Obtener grupos del usuario
            get {

                val currentUserId = call.userId()

                val groups = transaction {

                    (GroupsTable innerJoin GroupMembersTable)
                        .select { GroupMembersTable.user eq currentUserId }
                        .map { row ->

                            val groupId = row[GroupsTable.id]

                            val membersCount = GroupMembersTable
                                .select { GroupMembersTable.group eq groupId }
                                .count()
                                .toInt()

                            GroupResponse(
                                id = groupId,
                                name = row[GroupsTable.name],
                                createdBy = row[GroupsTable.createdBy],
                                createdAt = row[GroupsTable.createdAt].toString(),
                                memberCount = membersCount
                            )
                        }
                }

                call.respond(groups)
            }

            // Crear grupo
            post {
                val currentUserId = call.userId()
                val request = call.receive<CreateGroupRequest>()

                if (request.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "El nombre no puede estar vacío")
                    return@post
                }

                val groupId = newUniqueGroupId()

                transaction {
                    GroupsTable.insert {
                        it[id] = groupId
                        it[name] = request.name.trim()
                        it[createdBy] = currentUserId
                    }

                    GroupMembersTable.insert {
                        it[group] = groupId
                        it[user] = currentUserId
                    }
                }

                call.respond(HttpStatusCode.Created, CreateGroupResponse(groupId))
            }

            // Obtener grupo por ID
            get("/{groupId}") {

                val groupId = call.parameters["groupId"]
                    ?: return@get call.respondText("Missing groupId", status = HttpStatusCode.BadRequest)

                val group = transaction {
                    GroupsTable
                        .select { GroupsTable.id eq groupId }
                        .map {
                            GroupResponse(
                                id = it[GroupsTable.id],
                                name = it[GroupsTable.name],
                                createdBy = it[GroupsTable.createdBy],
                                createdAt = it[GroupsTable.createdAt].toString(),
                                memberCount = 0
                            )
                        }
                        .singleOrNull()
                }

                if (group == null) {
                    call.respond(HttpStatusCode.NotFound, "Grupo no encontrado")
                } else {
                    call.respond(group)
                }
            }

            // Calcular balances del grupo
            get("/{groupId}/balances") {

                val groupId = call.parameters["groupId"]!!

                try {
                    val balances = calculateGroupBalances(groupId)
                    call.respond(balances)

                } catch (e: Exception) {

                    e.printStackTrace() // 🔥 CLAVE

                    call.respond(
                        HttpStatusCode.InternalServerError,
                        e.message ?: "Error calculando balances"
                    )
                }
            }

            // Crear gasto
            post("/expenses") {

                val currentUserId = call.userId()
                val request = call.receive<CreateExpenseRequest>()

                if (request.description.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Descripción vacía")
                    return@post
                }

                if (request.amount <= 0) {
                    call.respond(HttpStatusCode.BadRequest, "El importe debe ser mayor que 0")
                    return@post
                }

                if (request.participants.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Debe haber al menos un participante")
                    return@post
                }

                val groupMembers = transaction {
                    GroupMembersTable
                        .select { GroupMembersTable.group eq request.groupId }
                        .map { it[GroupMembersTable.user] }
                }

                if (!request.participants.all { it in groupMembers }) {
                    call.respond(HttpStatusCode.BadRequest, "Participantes inválidos")
                    return@post
                }

                if (request.paidBy !in groupMembers) {
                    call.respond(HttpStatusCode.BadRequest, "El pagador no pertenece al grupo")
                    return@post
                }

                val expenseId = IdGenerator.newId8()

                transaction {

                    ExpensesTable.insert {
                        it[id] = expenseId
                        it[group] = request.groupId
                        it[title] = request.description
                        it[amount] = request.amount.toBigDecimal()
                        it[paidBy] = request.paidBy
                    }

                    val splitAmount = request.amount
                        .toBigDecimal()
                        .divide(request.participants.size.toBigDecimal(), 2, RoundingMode.HALF_UP)

                    request.participants.forEach { userId ->

                        ExpenseParticipantsTable.insert {
                            it[id] = IdGenerator.newId8()
                            it[expense] = expenseId
                            it[user] = userId
                            it[amount] = splitAmount
                        }
                    }
                }

                call.respond(HttpStatusCode.Created)
            }

            // Eliminar gasto
            delete("/expenses/{expenseId}") {

                val expenseId = call.parameters["expenseId"]
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("message" to "Missing expenseId")
                    )

                val exists = transaction {

                    ExpensesTable.select {
                        ExpensesTable.id eq expenseId
                    }.firstOrNull()
                }

                if (exists == null) {

                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("message" to "Gasto no encontrado")
                    )

                    return@delete
                }

                transaction {

                    ExpenseParticipantsTable.deleteWhere {
                        ExpenseParticipantsTable.expense eq expenseId
                    }

                    ExpensesTable.deleteWhere {
                        ExpensesTable.id eq expenseId
                    }
                }

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Gasto eliminado")
                )
            }

            // Salir del grupo
            post("/leave") {

                val currentUserId = call.userId()

                val request = call.receive<JoinGroupRequest>()

                val membership = transaction {

                    GroupMembersTable.select {
                        (GroupMembersTable.group eq request.groupId) and
                                (GroupMembersTable.user eq currentUserId)
                    }.firstOrNull()
                }

                if (membership == null) {

                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("message" to "No perteneces al grupo")
                    )

                    return@post
                }

                transaction {

                    GroupMembersTable.deleteWhere {
                        (GroupMembersTable.group eq request.groupId) and
                                (GroupMembersTable.user eq currentUserId)
                    }
                }

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Has salido del grupo")
                )
            }

            // Salir del grupo
            delete("/{groupId}/leave") {

                val currentUserId = call.userId()

                val groupId = call.parameters["groupId"]
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        "Missing groupId"
                    )

                transaction {

                    GroupMembersTable.deleteWhere {
                        (GroupMembersTable.group eq groupId) and
                                (GroupMembersTable.user eq currentUserId)
                    }
                }

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Has salido del grupo")
                )
            }

            // Obtener gastos del grupo
            get("/{groupId}/expenses") {

                val groupId = call.parameters["groupId"]
                    ?: return@get call.respondText(
                        "Missing groupId",
                        status = HttpStatusCode.BadRequest
                    )

                val expenses = transaction {

                    (ExpensesTable innerJoin UsersTable)
                        .select {
                            ExpensesTable.group eq groupId
                        }
                        .map { row ->

                            ExpenseResponse(
                                id = row[ExpensesTable.id],
                                groupId = row[ExpensesTable.group],
                                paidBy = row[ExpensesTable.paidBy],
                                paidByName = row[UsersTable.username],
                                title = row[ExpensesTable.title],
                                amount = row[ExpensesTable.amount].toDouble(),
                                createdAt = row[ExpensesTable.createdAt].toString()
                            )
                        }
                }

                call.respond(expenses)
            }

            // Calcular balances del grupo
            get("/{groupId}/balances") {

                val groupId = call.parameters["groupId"]
                    ?: return@get call.respondText(
                        "Missing groupId",
                        status = HttpStatusCode.BadRequest
                    )

                val balances = calculateGroupBalances(groupId)

                call.respond(balances)
            }

            // Obtener miembros
            get("/{groupId}/members") {

                val groupId = call.parameters["groupId"]
                    ?: return@get call.respondText("Missing groupId", status = HttpStatusCode.BadRequest)

                val members = transaction {
                    (GroupMembersTable innerJoin UsersTable)
                        .select { GroupMembersTable.group eq groupId }
                        .map {
                            GroupMemberResponse(
                                userId = it[UsersTable.id],
                                name = it[UsersTable.username],
                                email = it[UsersTable.email],
                                joinedAt = it[GroupMembersTable.joinedAt].toString()
                            )
                        }
                }

                call.respond(members)
            }

            // Unirse a grupo
            post("/join") {

                val currentUserId = call.userId()
                val request = call.receive<JoinGroupRequest>()

                val exists = transaction {
                    GroupsTable
                        .select { GroupsTable.id eq request.groupId }
                        .firstOrNull() != null
                }

                if (!exists) {
                    call.respond(HttpStatusCode.NotFound, "Grupo no existe")
                    return@post
                }

                val already = transaction {
                    GroupMembersTable
                        .select {
                            (GroupMembersTable.group eq request.groupId) and
                                    (GroupMembersTable.user eq currentUserId)
                        }
                        .firstOrNull() != null
                }

                if (already) {
                    call.respond(HttpStatusCode.Conflict, "Ya estás en el grupo")
                    return@post
                }

                transaction {
                    GroupMembersTable.insert {
                        it[group] = request.groupId
                        it[user] = currentUserId
                    }
                }

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Unido correctamente")
                )
            }
        }
    }
}

private fun newUniqueGroupId(): String {
    while (true) {
        val id = IdGenerator.newId8()
        val exists = transaction {
            GroupsTable
                .select { GroupsTable.id eq id }
                .firstOrNull() != null
        }
        if (!exists) return id
    }
}