package io.jadu.nstream.config

import io.ktor.server.application.ApplicationEnvironment

data class ServerConfig(
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val redisUri: String,
    val jwtSecret: String,
    val smtpHost: String,
    val smtpPort: Int,
    val migrateOnStart: Boolean,
    val smtpFrom: String,
)

object ServerConfigLoader {
    fun load(environment: ApplicationEnvironment) : ServerConfig = ServerConfig(
        databaseUrl = environment.requiredValue("nstream.database.url", "NSTREAM_DATABASE_URL"),
        databaseUser = environment.requiredValue("nstream.database.user", "NSTREAM_DATABASE_USER"),
        databasePassword = environment.requiredValue("nstream.database.password", "NSTREAM_DATABASE_PASSWORD"),
        redisUri = environment.requiredValue("nstream.redis.uri", "NSTREAM_REDIS_URI"),
        jwtSecret = environment.requiredValue("nstream.jwt.secret", "NSTREAM_JWT_SECRET"),
        smtpHost = environment.requiredValue("nstream.smtp.host", "NSTREAM_SMTP_HOST"),
        smtpPort = environment.requiredValue("nstream.smtp.port", "NSTREAM_SMTP_PORT").toIntOrNull()
            ?: error("Server configuration value NSTREAM_SMTP_PORT must be a number."),
        smtpFrom = environment.requiredValue("nstream.smtp.from", "NSTREAM_SMTP_FROM"),
        migrateOnStart = environment.optionalValue("nstream.database.migrate-on-start", "NSTREAM_DATABASE_MIGRATE_ON_START")
            ?.toBooleanStrictOrNull()
            ?: true,
        )

    private fun ApplicationEnvironment.requiredValue(propertyName: String, environmentName: String) : String =
        config.propertyOrNull(propertyName)?.getString()
            ?: System.getenv(environmentName)
            ?: error("Missing required server configuration $environmentName (or $propertyName)")

    private fun ApplicationEnvironment.optionalValue(propertyName: String, environmentName: String) : String? =
        config.propertyOrNull(propertyName)?.getString() ?: System.getenv(environmentName)
}