package io.jadu.nstream.api

import kotlinx.serialization.Serializable
import org.postgresql.core.Field

const val API_VERSION_PREFIX = "/api/v1"

@Serializable
data class ApiResponse<T>(
    val data: T,
    val requestId: String? = null
)

@Serializable
data class ValidationErrorResponse(
    val code: String = "validation_error",
    val message: String = "One or more fields are invalid",
    val fields: List<FieldValidationError>,
    val requestId: String? = null
)

@Serializable
data class FieldValidationError(
    val field: String,
    val message: String
)

@Serializable
data class PageMetadata(
    val limit: Int,
    val offset: Long,
    val returned: Int,
    val nextOffset: Long? = null,
)

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val page: PageMetadata,
    val requestId: String? = null,
)

fun pageMetadata(limit: Int, offset: Long, returned: Int): PageMetadata = PageMetadata(
    limit = limit,
    offset = offset,
    returned = returned,
    nextOffset = if (returned == limit) offset + returned else null,
)
