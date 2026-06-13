package com.fieldsync.visit

import com.fieldsync.auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/visits")
class VisitController(private val service: VisitService) {

    @GetMapping
    fun list(
        @AuthenticationPrincipal principal: AuthPrincipal,
        pageable: Pageable,
    ): Page<VisitResponse> =
        service.list(principal.orgId, pageable).map(VisitResponse::from)

    @GetMapping("/{id}")
    fun get(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
    ): VisitResponse =
        VisitResponse.from(service.get(principal.orgId, id))

    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody req: CreateVisitRequest,
    ): ResponseEntity<VisitResponse> {
        val created = service.create(principal.orgId, req)
        return ResponseEntity
            .created(URI.create("/api/visits/${created.id}"))
            .body(VisitResponse.from(created))
    }

    @PutMapping("/{id}")
    fun update(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody req: UpdateVisitRequest,
    ): VisitResponse =
        VisitResponse.from(service.update(principal.orgId, id, req))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")  // only managers can delete
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
    ) = service.delete(principal.orgId, id)
}
