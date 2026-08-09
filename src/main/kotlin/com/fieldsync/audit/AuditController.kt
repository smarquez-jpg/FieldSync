package com.fieldsync.audit

import com.fieldsync.auth.AuthPrincipal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/audit")
class AuditController(private val audit: AuditService) {
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    fun list(
        @AuthenticationPrincipal principal: AuthPrincipal,
        pageable: Pageable
    ): Page<AuditResponse> = audit.list(principal.orgId, pageable).map(AuditResponse::from)
}