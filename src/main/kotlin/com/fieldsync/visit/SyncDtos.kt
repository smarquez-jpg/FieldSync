package com.fieldsync.visit

import java.time.Instant
import java.util.UUID

enum class SyncStatus { APPLIED, CONFLICT, REJECTED }

/** One change the client is pushing up. */
data class SyncItem(
    val clientId: String,
    val baseVersion: Long? = null,   // null = client thinks this is a new, never-synced record
    val customerName: String,
    val notes: String? = null,
    val visitedAt: Instant,
)

/** A batch of changes. */
data class SyncRequest(
    val changes: List<SyncItem>,
)

/** The outcome for a single change. */
data class SyncResult(
    val clientId: String,
    val status: SyncStatus,
    val id: UUID? = null,
    val version: Long? = null,
    val server: VisitResponse? = null,   // filled on CONFLICT so the client can see the server's copy
    val reason: String? = null,          // filled on REJECTED
)

/** One result per change, same order in as out. */
data class SyncResponse(
    val results: List<SyncResult>,
)