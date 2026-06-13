package com.fieldsync.auth

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val auth: AuthService) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): AuthResponse =
        AuthResponse(auth.register(req))

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): AuthResponse =
        AuthResponse(auth.login(req))
}