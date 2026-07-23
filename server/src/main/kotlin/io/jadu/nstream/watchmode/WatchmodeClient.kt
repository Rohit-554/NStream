package io.jadu.nstream.watchmode

import io.jadu.nstream.config.ServerConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

class WatchModeClient(
    private val config: ServerConfig,
    private val cache: ContentResponseCache,
    private val http: HttpClient = createHttpClient()
) {

    private val json = Json { ignoreUnknownKeys = true }
    suspend fun listTitles(
        types: String,
        region: String?,
        page: Int
    ): WatchModeResults<ContentPage> =
        request(
            "list-titles",
            mapOf("types" to types, "page" to page.toString()) + optional("regions", region),
            "list-$types-$region-$page"
        ) {
            json.decodeFromString<WatchModeListDto>(it).toPage()
        }
    suspend fun search(query: String, types: String?): WatchModeResults<List<ContentSummary>> =
        request(
            "search",
            mapOf("search_field" to "name", "search_value" to query) + optional("types", types),
            "search-$query-$types"
        ) {
            json.decodeFromString<WatchModeSearchDto>(it).titleResults.map { result -> result.toSummary() }
        }


    suspend fun details(id: Long): WatchModeResults<ContentDetails> =
        request("title/$id/details", emptyMap(), "details-$id") {
            json.decodeFromString<WatchModeDetailsDto>(it).toDetails()
        }


    suspend fun sources(id: Long, region: String?) : WatchModeResults<List<StreamingSource>> =
        request("title/$id/sources",
            optional("regions", region),
            "sources-$id-$region"
            ){
            json.decodeFromString<List<WatchModeSourceDto>>(it).map { source ->
                StreamingSource(
                    source.id,
                    source.name,
                    source.type,
                    source.webUrl
                )
            }
        }

    private fun optional(key: String, value: String?) : Map<String, String> =
        value?.let{ mapOf(key to it)} ?: emptyMap()

    private suspend fun <T> request(
        path: String,
        parameters: Map<String, String>,
        cacheKey: String,
        mapper: (String) -> T
    ): WatchModeResults<T> {
        cache.read(cacheKey)?.let {
            return runCatching { WatchModeResults.Success(mapper(it)) }.getOrNull()
                ?: WatchModeResults.Unavailable
        }
        return try {
            val response = http.get("${config.watchModeBaseUrl.trimEnd('/')}/$path") {
                header("X-API-Key", config.watchModeApiKey)
                parameters.forEach { (key, value) -> parameter(key, value) }
            }
            when (response.status) {
                HttpStatusCode.OK -> response.bodyAsText().let { body ->
                    cache.write(
                        cacheKey,
                        body
                    ); WatchModeResults.Success(mapper(body))
                }

                HttpStatusCode.NotFound -> WatchModeResults.NotFound
                HttpStatusCode.TooManyRequests -> WatchModeResults.RateLimited
                else -> WatchModeResults.Unavailable
            }
        } catch (_: Exception) {
            WatchModeResults.Unavailable
        }
    }

    companion object {
        private fun createHttpClient() = HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 6_000;
                connectTimeoutMillis = 3_000;
                socketTimeoutMillis = 6_000
            }
        }
    }

}

sealed interface WatchModeResults<out T> {
    data class Success<T>(val value:T) : WatchModeResults<T>
    data object NotFound : WatchModeResults<Nothing>
    data object RateLimited : WatchModeResults<Nothing>
    data object Unavailable : WatchModeResults<Nothing>
}