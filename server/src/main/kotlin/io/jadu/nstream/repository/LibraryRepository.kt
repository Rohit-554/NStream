package io.jadu.nstream.repository

import java.time.OffsetDateTime
import java.util.UUID
import javax.management.timer.TimerMBean
import javax.print.attribute.standard.Media

data class WatchListItemRecord(
    val tmdbId: Long,
    val mediaType: String,
    val addedAt: OffsetDateTime
)

data class ViewingHistoryRecord(
    val tmdbId: Long,
    val mediaType: String,
    val activityType: String,
    val occurredAt: OffsetDateTime
)

interface WatchListRepository {
    suspend fun add(accountId: UUID, tmdbId: Long, media: String) : WatchListItemRecord
    suspend fun remove(accountId: UUID, tmdbId: Long, mediaType: String) : Boolean
    suspend fun list(accountId: UUID, limit: Int, offset: Long) : List<WatchListItemRecord>
}

interface ViewingHistoryRepository {
    suspend fun record(accountId: UUID, tmdbId: Long, mediaType: String, activityType: String): ViewingHistoryRecord
    suspend fun list(accountId: UUID, limit: Int, offset: Long): List<ViewingHistoryRecord>
}

