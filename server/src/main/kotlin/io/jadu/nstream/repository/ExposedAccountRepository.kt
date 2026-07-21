package io.jadu.nstream.repository

import io.jadu.nstream.database.tables.AccountsTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class ExposedAccountRepository : AccountRepository {
    override suspend fun findByEmail(email: String): AccountRecord? = databaseQuery{
        AccountsTable.selectAll().where { AccountsTable.email eq email }.singleOrNull()?.toAccountRecord()
    }

    override suspend fun findById(accountId: UUID): AccountRecord?  = databaseQuery {
        AccountsTable.selectAll().where { AccountsTable.id eq accountId }.singleOrNull()?.toAccountRecord()
    }

    override suspend fun create(
        email: String,
        passwordHash: String
    ): AccountRecord  = databaseQuery {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        AccountsTable.insert {
            it[AccountsTable.email] = email
            it[AccountsTable.passwordHash] = passwordHash
            it[createdAt] = now
            it[updatedAt] = now
        }.resultedValues!!.single().toAccountRecord()
    }

    override suspend fun markEmailVerified(accountId: UUID): Unit = databaseQuery {
        AccountsTable.update({ AccountsTable.id eq accountId }) {
            it[emailVerifiedAt] = OffsetDateTime.now(ZoneOffset.UTC)
            it[updatedAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }
    }

    override suspend fun updatePassword(accountId: UUID, passwordHash: String): Unit = databaseQuery {
        AccountsTable.update({ AccountsTable.id eq accountId}) {
            it[AccountsTable.passwordHash] = passwordHash
            it[updatedAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }
    }


    private fun ResultRow.toAccountRecord() = AccountRecord(
        id = this[AccountsTable.id].value,
        email = this[AccountsTable.email],
        passwordHash = this[AccountsTable.passwordHash],
        emailVerifiedAt = this[AccountsTable.emailVerifiedAt]
    )

    private suspend fun <T> databaseQuery(block: () -> T): T = withContext(Dispatchers.IO) {
        transaction { block() }
    }


}
