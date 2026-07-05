package com.fieldsync.visit

import com.fieldsync.auth.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sync")
class SyncController(private val syncService: SyncService) {

    @PostMapping
    fun sync(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody request: SyncRequest,
    ): SyncResponse = syncService.sync(principal.orgId, request)
}