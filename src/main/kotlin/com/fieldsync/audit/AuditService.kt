package com.fieldsync.audit

import com.fieldsync.auth.AuthPrincipal
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuditService(private val repo: AuditLogRepository) {
    fun record(action: AuditAction, entityType: String, entityId: UUID){
        val principal = SecurityContextHolder.getContext().authentication?.principal as? AuthPrincipal ?: return
        repo.save(AuditLog(
            orgId = principal.orgId,
            userId = principal.userId,
            entityId = entityId,
            action = action,
            entityType = entityType
        ))
    }

    @Transactional(readOnly = true)
    fun list(orgId: UUID, pageable: Pageable) =
        repo.findByOrgId(orgId, pageable)
}