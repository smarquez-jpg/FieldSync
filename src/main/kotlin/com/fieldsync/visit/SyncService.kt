package com.fieldsync.visit

import com.fieldsync.audit.AuditAction
import com.fieldsync.audit.AuditService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SyncService(private val repo: VisitRepository, private val audit: AuditService) {

    @Transactional
    fun sync(orgId: UUID, request: SyncRequest): SyncResponse =
        SyncResponse(request.changes.map { applyOne(orgId, it) })

    private fun applyOne(orgId: UUID, item: SyncItem): SyncResult {
        // Per-item validation: reject this one, don't fail the whole batch.
        if (item.customerName.isBlank()) {
            return SyncResult(item.clientId, SyncStatus.REJECTED, reason = "customerName must not be blank")
        }

        val existing = repo.findByOrgIdAndClientId(orgId, item.clientId)

        return when {
            // 1. Brand-new record.
            existing == null -> {
                val created = repo.saveAndFlush(
                    Visit(
                        clientId = item.clientId,
                        orgId = orgId,
                        customerName = item.customerName,
                        notes = item.notes,
                        visitedAt = item.visitedAt,
                    )
                )
                audit.record(action = AuditAction.CREATE, entityType = "Visit", entityId = created.id)
                SyncResult(item.clientId, SyncStatus.APPLIED, id = created.id, version = created.version)
            }

            // 2. Replay of a create we already stored -> idempotent no-op.
            item.baseVersion == null ->
                SyncResult(item.clientId, SyncStatus.APPLIED, id = existing.id, version = existing.version)

            // 3. Client edited the current version -> safe to update.
            item.baseVersion == existing.version -> {
                existing.customerName = item.customerName
                existing.notes = item.notes
                existing.visitedAt = item.visitedAt
                val saved = repo.saveAndFlush(existing)
                audit.record(action = AuditAction.UPDATE, entityType = "Visit", entityId = saved.id)
                SyncResult(item.clientId, SyncStatus.APPLIED, id = saved.id, version = saved.version)
            }

            // 4. Client edited a stale version -> conflict, don't overwrite.
            else -> SyncResult(
                item.clientId,
                SyncStatus.CONFLICT,
                id = existing.id,
                version = existing.version,
                server = VisitResponse.from(existing),
            )
        }
    }
}