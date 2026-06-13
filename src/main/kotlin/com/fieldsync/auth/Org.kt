package com.fieldsync.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "orgs")
class Org (
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,
)