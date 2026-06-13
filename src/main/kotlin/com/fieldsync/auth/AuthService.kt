package com.fieldsync.auth

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class AuthService(
    private val users: UserRepository,
    private val orgs: OrgRepository,
    private val encoder: PasswordEncoder,
    private val jwt: JwtService,
) {
    fun register(req: RegisterRequest): String {
        if (users.existsByEmail(req.email)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email already registered")
        }
        val org = orgs.save(Org(name = req.orgName))
        val user = users.save(
            User(
                orgId = org.id,
                email = req.email,
                passwordHash = encoder.encode(req.password),  // hash, never the raw password
                role = Role.MANAGER,                          // the person who creates an org runs it
            )
        )
        return jwt.issue(user)
    }

    fun login(req: LoginRequest): String {
        val user = users.findByEmail(req.email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        if (!encoder.matches(req.password, user.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }
        return jwt.issue(user)
    }
}