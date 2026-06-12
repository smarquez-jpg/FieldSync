package com.fieldsync.visit

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class CreateVisitRequest(
    @field:NotBlank val clientId: String,
    @field:NotBlank val customerName: String,
    val notes: String? = null,
    @field:NotNull val visitedAt: Instant,
)

data class UpdateVisitRequest(
    @field:NotBlank val customerName: String,
    val notes: String? = null,
    @field:NotNull val visitedAt: Instant,
)

data class VisitResponse(
    val id: UUID,
    val clientId: String,
    val customerName: String,
    val notes: String?,
    val visitedAt: Instant,
    val version: Long,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    companion object {
        fun from(v: Visit) = VisitResponse(
            id = v.id,
            clientId = v.clientId,
            customerName = v.customerName,
            notes = v.notes,
            visitedAt = v.visitedAt,
            version = v.version,
            createdAt = v.createdAt,
            updatedAt = v.updatedAt,
        )
    }
}

class VisitNotFoundException(id: UUID) :
    com.fieldsync.common.NotFoundException("Visit $id not found")
