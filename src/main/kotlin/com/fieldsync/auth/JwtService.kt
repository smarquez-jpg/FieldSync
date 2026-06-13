package com.fieldsync.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${security.jwt.secret}") secret: String,
    @Value("\${security.jwt.ttl-seconds:3600}") private val ttlSeconds: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    /** Build a signed token for a user, embedding their org and role as claims. */
    fun issue(user: User): String {
        val now = Date()
        return Jwts.builder()
            .subject(user.id.toString())
            .claim("orgId", user.orgId.toString())
            .claim("role", user.role.name)
            .issuedAt(now)
            .expiration(Date(now.time + ttlSeconds * 1000))
            .signWith(key)
            .compact()
    }

    /** Verify a token's signature + expiry, then read its claims back out. */
    fun parse(token: String): AuthPrincipal {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
        return AuthPrincipal(
            userId = UUID.fromString(claims.subject),
            orgId = UUID.fromString(claims["orgId"] as String),
            role = Role.valueOf(claims["role"] as String),
        )
    }
}

/** The trusted identity we pull out of a valid token. */
data class AuthPrincipal(
    val userId: UUID,
    val orgId: UUID,
    val role: Role,
)