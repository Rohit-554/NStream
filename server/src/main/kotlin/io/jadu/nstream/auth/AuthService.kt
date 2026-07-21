package io.jadu.nstream.auth

import io.jadu.nstream.EmailSender
import io.jadu.nstream.repository.AccountRecord
import io.jadu.nstream.repository.AccountRepository
import io.jadu.nstream.repository.AccountTokenRepository
import io.jadu.nstream.repository.RefreshSessionRepository
import org.flywaydb.core.internal.util.BooleanEvaluator
import java.lang.reflect.Field
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Locale
import java.util.UUID

class AuthService(
    private val accountRepository: AccountRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenService: TokenService,
    private val refreshSessionRepository: RefreshSessionRepository,
    private val accountTokenRepository: AccountTokenRepository,
    private val emailSender: EmailSender
) {

    suspend fun signUp(email: String, password: String): AuthResult<AccountRecord> {
        val validationErrors = validateCredentials(email, password)
        if (validationErrors.isNotEmpty()) return AuthResult.Invalid(validationErrors)

        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        if (accountRepository.findByEmail(normalizedEmail) != null) {
            return AuthResult.Invalid(listOf(FieldError("email", "An account with this email already exists.")))
        }
        val account = accountRepository.create(normalizedEmail, passwordHasher.hash(password))
        sendVerification(account)
        return AuthResult.Success(account)
    }

    suspend fun login(email: String, password: String) : LoginResult {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        if(normalizedEmail.isBlank() || password.isBlank()) return LoginResult.InvalidCredentials

        val account = accountRepository.findByEmail(normalizedEmail) ?: return LoginResult.InvalidCredentials
        if(!passwordHasher.matches(password, account.passwordHash)) return LoginResult.InvalidCredentials
        if(account.emailVerifiedAt == null) return LoginResult.EmailNotVerified
        val refreshToken = tokenService.createOpaqueToken()
        refreshSessionRepository.create(
            accountId = account.id,
            tokenHash = tokenService.hashOpaqueToken(refreshToken),
            expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusDays(30)
        )
        return LoginResult.Success(
            account,
            SessionToken(
                tokenService.createAccessToken(account),
                refreshToken
            )
        )

    }

    suspend fun requestVerification(email: String) {
        accountRepository.findByEmail(
            email.trim().lowercase(Locale.ROOT)
        )?.let { sendVerification(it) }
    }

    suspend fun confirmVerification(token: String) : Boolean {
        val record = accountTokenRepository.useVerification(tokenService.hashOpaqueToken(token)) ?: return false
        accountRepository.markEmailVerified(record.accountId)
        return true
    }

    suspend fun requestPasswordReset(email: String) {
        accountRepository.findByEmail(email.trim().lowercase(Locale.ROOT))?.let { account ->
            val token = tokenService.createOpaqueToken()
            accountTokenRepository.createReset(
                account.id,
                tokenService.hashOpaqueToken(token),
                expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(30)
            )
            emailSender.send(account.email, "Reset your password", "Your Password reset token is $token")
        }
    }

    suspend fun resetPassword(token: String, password: String): Boolean {
        if(password.length<12) return false
        val record = accountTokenRepository.useReset(
            tokenService.hashOpaqueToken(token)
        )?: return false
        accountRepository.updatePassword(record.accountId, passwordHasher.hash(password))
        refreshSessionRepository.revokeAllForAccount(record.accountId)
        return true
    }

    suspend fun refresh(refreshToken: String) : SessionToken? {
        val session = refreshSessionRepository.findActive(tokenService.hashOpaqueToken(refreshToken)) ?: return null
        val account = accountRepository.findById(session.accountId) ?: return null
        refreshSessionRepository.revoke(session.id)
        val replacement = tokenService.createOpaqueToken()
        refreshSessionRepository.create(
            account.id,
            tokenService.hashOpaqueToken(replacement),
            OffsetDateTime.now(ZoneOffset.UTC).plusDays(30)
        )
        return SessionToken(
            tokenService.createAccessToken(account),
            replacement
        )

    }

    suspend fun logout(refreshToken: String) {
        refreshSessionRepository.findActive(
            tokenService.hashOpaqueToken(refreshToken)
        )?.let {
            refreshSessionRepository.revoke(it.id)
        }
    }

    suspend fun currentAccount(accountId : String) : AccountRecord? = runCatching {
        UUID.fromString(accountId)
    } .getOrNull()?.let { accountRepository.findById(it) }

    private suspend fun sendVerification(account: AccountRecord) {
        val token = tokenService.createOpaqueToken()
        accountTokenRepository.createVerification(
            accountId = account.id,
            tokenHash = tokenService.hashOpaqueToken(token),
            expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(24)
        )
        emailSender.send(
            account.email,
            "Verify your Email",
            "Your Verification token is $token"
        )
    }

    private fun validateCredentials(email: String, password: String) : List<FieldError> = buildList {
        if(!email.trim().contains('@')) add(FieldError("email", "Enter a valid email address"))
        if(password.length<12) add(FieldError("password", "Password must be least 12 characters."))
    }

}

data class FieldError(
    val field: String,
    val message : String
)

data class SessionToken (
    val accessTokens: String,
    val refreshTokens: String
)

sealed interface AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>
    data class Invalid(val errors: List<FieldError>) : AuthResult<Nothing>
}

sealed interface LoginResult {
    data class Success(val account: AccountRecord, val tokens: SessionToken) : LoginResult
    data object InvalidCredentials : LoginResult
    data object EmailNotVerified : LoginResult
}