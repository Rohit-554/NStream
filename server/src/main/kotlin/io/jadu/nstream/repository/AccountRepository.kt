package io.jadu.nstream.repository

import java.time.OffsetDateTime
import java.util.UUID

data class AccountRecord(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val emailVerifiedAt: OffsetDateTime?
)

interface AccountRepository {
    suspend fun findByEmail(email: String) : AccountRecord?
    suspend fun findById(accountId: UUID): AccountRecord?
    suspend fun create(email: String, passwordHash: String): AccountRecord
    suspend fun markEmailVerified(accountId: UUID)
    suspend fun updatePassword(accountId: UUID, passwordHash: String)
}