package io.jadu.nstream.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object WatchlistItemsTable : UUIDTable("watchlist_items") {
    val accountId = reference("account_id", AccountsTable)
    val watchModeTitleId = long("watch_mode_title_id")
    val mediaType = varchar("media_type", 16)
    val addedAt = timestampWithTimeZone("added_at")

    init {
        uniqueIndex(accountId, watchModeTitleId, mediaType)
    }
}

object ViewingHistoryTable : UUIDTable("viewing_history") {
    val accountId = reference("account_id", AccountsTable)
    val watchModeTitleId = long("watch_mode_title_id")
    val mediaType = varchar("media_type", 16)
    val activityType = varchar("activity_type", 32)
    val occurredAt = timestampWithTimeZone("occurred_at")
}
