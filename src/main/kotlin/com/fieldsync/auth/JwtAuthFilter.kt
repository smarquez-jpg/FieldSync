package com.fieldsync.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwt: JwtService) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ").trim()
            try {
                val principal = jwt.parse(token)
                // "ROLE_" prefix is the convention Spring uses for hasRole(...) checks in Step 6.
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${principal.role.name}"))
                val auth = UsernamePasswordAuthenticationToken(principal, null, authorities)
                SecurityContextHolder.getContext().authentication = auth
            } catch (ex: Exception) {
                // Bad/expired token: leave the request unauthenticated; it'll be rejected.
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }

}