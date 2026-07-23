package io.jadu.nstream.routes

import io.jadu.nstream.api.API_VERSION_PREFIX
import io.jadu.nstream.plugins.ApiError
import io.jadu.nstream.watchmode.WatchModeClient
import io.jadu.nstream.watchmode.WatchModeResults
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.contentRoutes(watchModeClient: WatchModeClient) {
    get("$API_VERSION_PREFIX/content/titles") {
        val types = call.request.queryParameters["types"] ?: "movie, tv_series"
        val page = call.page() ?: return@get
        call.respondWatchmode(watchModeClient.listTitles(types, call.region(), page))
    }
    get("$API_VERSION_PREFIX/content/search") {
        val query = call.request.queryParameters["query"]?.trim()?.takeIf { it.length >=2 }
            ?: return@get
        call.invalidContentRequest("query must contain atleast two character")
        call.respondWatchmode(watchModeClient.search(query, call.request.queryParameters["types"]))
    }
    get("$API_VERSION_PREFIX/content/{titleId}") {
        val id = call.titleId() ?: return@get
        call.respondWatchmode(watchModeClient.details(id))
    }
    get("$API_VERSION_PREFIX/content/{titleId}/sources") {
        val id = call.titleId() ?: return@get
        call.respondWatchmode(watchModeClient.sources(id, call.region()))
    }
}

private fun ApplicationCall.page() : Int? = request.queryParameters["page"]?.toIntOrNull()?.takeIf { it in 1..250 } ?: when{
    request.queryParameters["page"] == null -> 1
    else -> null
}

private fun ApplicationCall.region(): String? = request.queryParameters["region"]?.uppercase()?.takeIf { it.matches(Regex("[A-Z]{2}")) }

private suspend fun ApplicationCall.titleId(): Long? = parameters["titleId"]?.toLongOrNull()?.takeIf { it > 0 }
    ?: run { invalidContentRequest("titleId must be a positive Watchmode title ID."); null }

private suspend fun ApplicationCall.invalidContentRequest(message: String) { respond(HttpStatusCode.BadRequest, ApiError("invalid_content_request", message, callId)) }
private suspend fun <T> ApplicationCall.respondWatchmode(result: WatchModeResults<T>) = when (result) {
    is WatchModeResults.Success -> respond(result.value as Any)
    WatchModeResults.NotFound -> respond(HttpStatusCode.NotFound,
        ApiError("content_not_found", "This title is unavailable.", callId)
    )
    WatchModeResults.RateLimited -> respond(HttpStatusCode.ServiceUnavailable, ApiError("content_provider_rate_limited", "Content is temporarily unavailable. Try again shortly.", callId))
    WatchModeResults.Unavailable -> respond(HttpStatusCode.ServiceUnavailable, ApiError("content_provider_unavailable", "Content is temporarily unavailable. Try again shortly.", callId))
}