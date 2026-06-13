package com.fieldsync.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "users")
class User (
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "org_id", nullable = false)
    val orgId: UUID,

    @Column(nullable = false)
    val email: String,

    // We store a BCrypt *hash*, never the raw password. More on that in Step 3.
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role,
)