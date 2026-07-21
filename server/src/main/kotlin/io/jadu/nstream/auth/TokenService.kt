package io.jadu.nstream.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.jadu.nstream.config.ServerConfig
import io.jadu.nstream.repository.AccountRecord
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.Date

// to see how access tokens and opaque refresh tokens are created and hashed.

class TokenService(config: ServerConfig) {
    // Hash-based Message Authentication Code.

    private val algorithm = Algorithm.HMAC256(config.jwtSecret)

    fun createAccessToken(account: AccountRecord): String = JWT.create()
        .withSubject(account.id.toString())
        .withClaim("email", account.email)
        .withIssuedAt(Date.from(Instant.now()))
        .withExpiresAt(Date.from(Instant.now().plusSeconds(15 * 60)))
        .sign(algorithm)

    fun createOpaqueToken(): String =
        Base64.getUrlEncoder()
            .withoutPadding() // =
            .encodeToString(
                ByteArray(
                    48
                ).also(random::nextBytes)
            )

    fun hashOpaqueToken(token: String) : String = MessageDigest.getInstance("SHA-256")
        .digest(token.toByteArray(Charsets.UTF_8))
        .joinToString(""){ "%02x".format(it)}

    private val random = SecureRandom()
}