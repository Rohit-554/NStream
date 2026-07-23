package io.jadu.nstream

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.jadu.nstream.config.ServerConfigLoader
import io.jadu.nstream.database.DatabaseMigrations
import io.jadu.nstream.di.serverModule
import io.jadu.nstream.plugins.configureHttp
import io.jadu.nstream.routes.authRoutes
import io.jadu.nstream.routes.contentRoutes
import io.jadu.nstream.routes.systemRoute
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val config = ServerConfigLoader.load(environment)
    DatabaseMigrations.connect(config)
    if(config.migrateOnStart) {
        DatabaseMigrations.migrate(config)
    }
    install(Koin){
        modules(serverModule(config))
    }
    configureHttp()
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JWT.require(Algorithm.HMAC256(config.jwtSecret)).build())
            validate { credentials -> credentials.payload.subject?.let { JWTPrincipal(credentials.payload) } }
        }
    }
    routing {
        systemRoute(config)
        authRoutes(getKoin().get(), getKoin().get())
        contentRoutes(getKoin().get())
    }
}