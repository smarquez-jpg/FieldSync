package com.fieldsync.visit

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class VisitService(private val repo: VisitRepository) {

    @Transactional(readOnly = true)
    fun list(orgId: UUID, pageable: Pageable): Page<Visit> =
        repo.findByOrgId(orgId, pageable)

    @Transactional(readOnly = true)
    fun get(orgId: UUID, id: UUID): Visit =
        repo.findByIdAndOrgId(id, orgId) ?: throw VisitNotFoundException(id)

    fun create(orgId: UUID, req: CreateVisitRequest): Visit {
        val visit = Visit(
            clientId = req.clientId,
            orgId = orgId,
            customerName = req.customerName,
            notes = req.notes,
            visitedAt = req.visitedAt,
        )
        return repo.save(visit)
    }

    fun update(orgId: UUID, id: UUID, req: UpdateVisitRequest): Visit {
        val visit = get(orgId, id)
        visit.customerName = req.customerName
        visit.notes = req.notes
        visit.visitedAt = req.visitedAt
        return repo.save(visit)
    }

    fun delete(orgId: UUID, id: UUID) {
        repo.delete(get(orgId, id))
    }
}
