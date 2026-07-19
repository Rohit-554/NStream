package io.jadu.nstream.routes

import io.jadu.nstream.config.ServerConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import jdk.jfr.StackTrace
import kotlinx.serialization.Serializable
import io.ktor.server.routing.get

@Serializable
data class ServiceStatus(val status: String)

fun Route.systemRoute(config: ServerConfig) {
    get("/health") {
        call.respond(HttpStatusCode.OK, ServiceStatus(status = "ok"))
    }
    get("/ready") {
        val isConfigured = config.databaseUrl.isNotBlank() && config.redisUri.isNotBlank()
        call.respond(
            if(isConfigured) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable,
            ServiceStatus(if (isConfigured) "ready" else "not_ready")
        )
    }
    swaggerUI(path = "swagger", swaggerFile = "openapi/openapi.yaml")
}