package com.fieldsync.auth

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrgRepository : JpaRepository<Org, UUID>

interface UserRepository: JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}
