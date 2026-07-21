package io.jadu.nstream.repository

import io.jadu.nstream.database.tables.RefreshSessionsTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

data class RefreshSessionRecord(
    val id: UUID,
    val accountId: UUID,
    val tokenHash: String,
    val expiresAt: OffsetDateTime,
    val revokedAt: OffsetDateTime?
)

interface RefreshSessionRepository {
    suspend fun create(accountId: UUID, tokenHash: String, expiresAt: OffsetDateTime) : RefreshSessionRecord
    suspend fun findActive(tokenHash: String): RefreshSessionRecord?
    suspend fun revoke(sessionId: UUID)
    suspend fun revokeAllForAccount(accountId: UUID)
}

class ExposedRefreshSessionRepository: RefreshSessionRepository {
    override suspend fun create(
        accountId: UUID,
        tokenHash: String,
        expiresAt: OffsetDateTime
    ): RefreshSessionRecord  = query {
        RefreshSessionsTable.insert {
            it[RefreshSessionsTable.accountId] = accountId
            it[RefreshSessionsTable.tokenHash] = tokenHash
            it[RefreshSessionsTable.expiresAt] = expiresAt
            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }.resultedValues!!.single().let { row ->
            RefreshSessionRecord(
                id = row[RefreshSessionsTable.id].value,
                accountId = row[RefreshSessionsTable.accountId].value,
                tokenHash = row[RefreshSessionsTable.tokenHash],
                expiresAt = row[RefreshSessionsTable.expiresAt],
                revokedAt = row[RefreshSessionsTable.revokedAt]
            )
        }
    }

    private suspend fun <T> query(block: () -> T): T = withContext(Dispatchers.IO) { transaction { block() } }


    override suspend fun findActive(tokenHash: String): RefreshSessionRecord? = query {
        RefreshSessionsTable.selectAll().where { RefreshSessionsTable.tokenHash eq tokenHash }
            .singleOrNull()
            ?.let { row -> RefreshSessionRecord(row[RefreshSessionsTable.id].value, row[RefreshSessionsTable.accountId].value, row[RefreshSessionsTable.tokenHash], row[RefreshSessionsTable.expiresAt], row[RefreshSessionsTable.revokedAt]) }
            ?.takeIf { it.revokedAt == null && it.expiresAt.isAfter(OffsetDateTime.now(ZoneOffset.UTC)) }
    }

    override suspend fun revoke(sessionId: UUID) {
        RefreshSessionsTable.update({ RefreshSessionsTable.id eq sessionId }) {
            it[revokedAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }
    }

    override suspend fun revokeAllForAccount(accountId: UUID): Unit = query {
        RefreshSessionsTable.update({ RefreshSessionsTable.accountId eq accountId }) {
            it[revokedAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }
    }

}