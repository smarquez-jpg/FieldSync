package com.fieldsync.visit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "visits")
class Visit(

    @Id
    val id: UUID = UUID.randomUUID(),

    // Client-generated id; the basis for idempotent sync in M3.
    @Column(name = "client_id", nullable = false)
    val clientId: String,

    // Tenant scope. Hard-coded to a demo org in M1; comes from the JWT in M2.
    @Column(name = "org_id", nullable = false)
    val orgId: UUID,

    @Column(name = "customer_name", nullable = false)
    var customerName: String,

    @Column(name = "notes")
    var notes: String? = null,

    @Column(name = "visited_at", nullable = false)
    var visitedAt: Instant,

    // Optimistic-locking version; used for conflict detection in M3.
    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null,
)
