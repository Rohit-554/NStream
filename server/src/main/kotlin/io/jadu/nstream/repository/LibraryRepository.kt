package io.jadu.nstream.repository

import java.time.OffsetDateTime
import java.util.UUID

data class WatchListItemRecord(
    val watchModeTitleId: Long,
    val mediaType: String,
    val addedAt: OffsetDateTime
)

data class ViewingHistoryRecord(
    val watchModeTitleId: Long,
    val mediaType: String,
    val activityType: String,
    val occurredAt: OffsetDateTime
)

interface WatchListRepository {
    suspend fun add(accountId: UUID, watchModelTitleId: Long, media: String) : WatchListItemRecord
    suspend fun remove(accountId: UUID, watchModelTitleId: Long, mediaType: String) : Boolean
    suspend fun list(accountId: UUID, limit: Int, offset: Long) : List<WatchListItemRecord>
}

interface ViewingHistoryRepository {
    suspend fun record(accountId: UUID, watchModelTitleId: Long, mediaType: String, activityType: String): ViewingHistoryRecord
    suspend fun list(accountId: UUID, limit: Int, offset: Long): List<ViewingHistoryRecord>
}

