package io.jadu.nstream.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object AccountsTable : UUIDTable("accounts") {
    val email = varchar("email", 320).uniqueIndex()
    val passwordHash = text("password_hash")
    val emailVerifiedAt = timestampWithTimeZone("email_verified_at").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

object RefreshSessionsTable : UUIDTable("refresh_sessions") {
    val accountId = reference("account_id", AccountsTable)
    val tokenHash = text("token_hash").uniqueIndex()
    val expiresAt = timestampWithTimeZone("expires_at")
    val revokedAt = timestampWithTimeZone("revoked_at").nullable()
    val replacedBySessionId = uuid("replaced_by_session_id").nullable()
    val createdAt = timestampWithTimeZone("created_at")
}

object EmailVerificationTokensTable : UUIDTable("email_verification_tokens") {
    val accountId = reference("account_id", AccountsTable)
    val tokenHash = text("token_hash").uniqueIndex()
    val expiresAt = timestampWithTimeZone("expires_at")
    val usedAt = timestampWithTimeZone("used_at").nullable()
    val createdAt = timestampWithTimeZone("created_at")
}

object PasswordResetTokensTable : UUIDTable("password_reset_tokens") {
    val accountId = reference("account_id", AccountsTable)
    val tokenHash = text("token_hash").uniqueIndex()
    val expiresAt = timestampWithTimeZone("expires_at")
    val usedAt = timestampWithTimeZone("used_at").nullable()
    val createdAt = timestampWithTimeZone("created_at")
}
