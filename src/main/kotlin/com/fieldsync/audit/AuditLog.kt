package com.fieldsync.audit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audit_log")
class AuditLog(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "org_id", nullable = false)
    val orgId: UUID,

    @Column(name = "user_id")
    val userId: UUID?,

    @Enumerated(EnumType.STRING)
    val action: AuditAction,

    @Column(name = "entity_type", nullable = false)
    val entityType: String,

    @Column(name = "entity_id", nullable = false)
    val entityId: UUID,

    @Column(name = "occurred_at", insertable = false, updatable = false)
    val occurredAt: Instant? = null,
)