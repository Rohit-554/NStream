package io.jadu.nstream.repository

import io.jadu.nstream.database.tables.EmailVerificationTokensTable
import io.jadu.nstream.database.tables.PasswordResetTokensTable
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
import kotlin.math.exp

data class AccountTokenRecord(
    val id: UUID,
    val accountId: UUID,
    val tokenHash: String,
    val expiresAt: OffsetDateTime,
    val usedAt: OffsetDateTime?
)

interface AccountTokenRepository {
    suspend fun createVerification(accountId: UUID, tokenHash: String, expiresAt: OffsetDateTime)
    suspend fun useVerification(tokenHash: String): AccountTokenRecord?
    suspend fun createReset(accountId: UUID, tokenHash: String, expiresAt: OffsetDateTime)
    suspend fun useReset(tokenHash: String): AccountTokenRecord?
}

class ExposedAccountTokenRepository : AccountTokenRepository {
    override suspend fun createVerification(
        accountId: UUID,
        tokenHash: String,
        expiresAt: OffsetDateTime
    ): Unit = query {
        EmailVerificationTokensTable.insert {
            it[EmailVerificationTokensTable.accountId] = accountId
            it[EmailVerificationTokensTable.tokenHash] = tokenHash
            it[EmailVerificationTokensTable.expiresAt] = expiresAt
            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }
    }

    override suspend fun useVerification(tokenHash: String): AccountTokenRecord?  = query {
        val row = EmailVerificationTokensTable.selectAll()
            .where{ EmailVerificationTokensTable.tokenHash eq tokenHash }.singleOrNull() ?: return@query null

        if(row[EmailVerificationTokensTable.usedAt] != null || !row[EmailVerificationTokensTable.expiresAt].isAfter(
                OffsetDateTime.now(ZoneOffset.UTC)
        )) return@query null

        EmailVerificationTokensTable.update({ EmailVerificationTokensTable.id eq row[EmailVerificationTokensTable.id] }) {
            it[usedAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }

        AccountTokenRecord(
            row[EmailVerificationTokensTable.id].value,
            row[EmailVerificationTokensTable.accountId].value,
            tokenHash,
            row[EmailVerificationTokensTable.expiresAt],
            null
        )
    }

    override suspend fun createReset(
        accountId: UUID,
        tokenHash: String,
        expiresAt: OffsetDateTime
    ) : Unit = query {
        PasswordResetTokensTable.insert {
            it[PasswordResetTokensTable.accountId] = accountId
            it[PasswordResetTokensTable.tokenHash] = tokenHash
            it[PasswordResetTokensTable.expiresAt] = expiresAt
            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }
    }

    override suspend fun useReset(tokenHash: String): AccountTokenRecord? = query {
        val row = PasswordResetTokensTable.selectAll()
            .where { PasswordResetTokensTable.tokenHash eq tokenHash }.singleOrNull()
            ?: return@query null
        if (row[PasswordResetTokensTable.usedAt] != null || !row[PasswordResetTokensTable.expiresAt].isAfter(
                OffsetDateTime.now(ZoneOffset.UTC)
            )
        ) return@query null
        PasswordResetTokensTable.update({ PasswordResetTokensTable.id eq row[PasswordResetTokensTable.id] }) {
            it[usedAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }
        AccountTokenRecord(
            row[PasswordResetTokensTable.id].value,
            row[PasswordResetTokensTable.accountId].value,
            tokenHash,
            row[PasswordResetTokensTable.expiresAt],
            null
        )
    }

    private suspend fun <T> query(block: () -> T): T = withContext(Dispatchers.IO) { transaction { block() } }

}

