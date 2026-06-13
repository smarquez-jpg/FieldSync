package com.fieldsync.visit

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class VisitControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")

        @DynamicPropertySource
        @JvmStatic
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    /** Register a fresh org/user and return a usable Bearer token. */
    private fun registerAndGetToken(email: String): String {
        val body = """{"email":"$email","password":"password123","orgName":"Test Org"}"""
        val json = mockMvc.perform(
            post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        return objectMapper.readTree(json).get("token").asText()
    }

    @Test
    fun `rejects requests without a token`() {
        mockMvc.perform(get("/api/visits"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `creates and lists a visit for the authenticated org`() {
        val token = registerAndGetToken("creator@example.com")
        val body = """{"clientId":"c-1","customerName":"Acme Clinic","visitedAt":"2026-01-01T10:00:00Z"}"""

        mockMvc.perform(
            post("/api/visits")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.customerName").value("Acme Clinic"))

        mockMvc.perform(
            get("/api/visits").header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
    }

    @Test
    fun `does not leak visits across orgs`() {
        val tokenA = registerAndGetToken("org-a@example.com")
        val tokenB = registerAndGetToken("org-b@example.com")

        // Org A creates a visit
        mockMvc.perform(
            post("/api/visits")
                .header("Authorization", "Bearer $tokenA")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"clientId":"a-1","customerName":"A Customer","visitedAt":"2026-01-01T10:00:00Z"}""")
        ).andExpect(status().isCreated)

        // Org B must not see it
        mockMvc.perform(
            get("/api/visits").header("Authorization", "Bearer $tokenB")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(0))
    }

    @Test
    fun `rejects an invalid payload`() {
        val token = registerAndGetToken("validator@example.com")
        mockMvc.perform(
            post("/api/visits")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"notes":"x"}""")
        )
            .andExpect(status().isBadRequest)
    }
}