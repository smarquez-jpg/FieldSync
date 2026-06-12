package com.fieldsync.visit

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface VisitRepository : JpaRepository<Visit, UUID> {
    fun findByOrgId(orgId: UUID, pageable: Pageable): Page<Visit>
    fun findByIdAndOrgId(id: UUID, orgId: UUID): Visit?
}
