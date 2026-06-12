package com.fieldsync.visit

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

    // TODO(M2): replace with the authenticated user's org, read from the JWT.
    private val demoOrg: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")

    @GetMapping
    fun list(pageable: Pageable): Page<VisitResponse> =
        service.list(demoOrg, pageable).map(VisitResponse::from)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): VisitResponse =
        VisitResponse.from(service.get(demoOrg, id))

    @PostMapping
    fun create(@Valid @RequestBody req: CreateVisitRequest): ResponseEntity<VisitResponse> {
        val created = service.create(demoOrg, req)
        return ResponseEntity
            .created(URI.create("/api/visits/${created.id}"))
            .body(VisitResponse.from(created))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody req: UpdateVisitRequest): VisitResponse =
        VisitResponse.from(service.update(demoOrg, id, req))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) = service.delete(demoOrg, id)
}
