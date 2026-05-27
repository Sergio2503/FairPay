package com.fairpay.database

import com.fairpay.database.tables.ExpenseParticipantsTable
import com.fairpay.database.tables.ExpensesTable
import com.fairpay.database.tables.GroupMembersTable
import com.fairpay.database.tables.GroupsTable
import com.fairpay.database.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.ApplicationEnvironment
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object DatabaseFactory {

    fun init(environment: ApplicationEnvironment) {
        val dataSource = hikari(environment)

        Database.connect(dataSource)
        transaction {
            SchemaUtils.create(
                UsersTable,
                GroupsTable,
                GroupMembersTable,
                ExpensesTable,
                ExpenseParticipantsTable
            )
        }
    }

    private fun hikari(environment: ApplicationEnvironment): DataSource {
        val config = HikariConfig().apply {
            jdbcUrl = environment.config.property("database.url").getString()
            driverClassName = environment.config.property("database.driver").getString()
            username = environment.config.property("database.user").getString()
            password = environment.config.property("database.password").getString()
            maximumPoolSize = environment.config.property("database.maximumPoolSize").getString().toInt()

            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            connectionTimeout = 30000
            initializationFailTimeout = 60000
            validationTimeout = 5000
        }

        return HikariDataSource(config)
    }
}