package com.fieldsync.audit

import java.time.Instant
import java.util.UUID

data class AuditResponse(
    val id: UUID,
    val userId: UUID?,
    val action: AuditAction,
    val entityType: String,
    val entityId: UUID,
    val occurredAt: Instant?,
) {
    companion object {
        fun from(a: AuditLog) = AuditResponse(
            id = a.id,
            userId = a.userId,
            action = a.action,
            entityType = a.entityType,
            entityId = a.entityId,
            occurredAt = a.occurredAt
        )
    }
}