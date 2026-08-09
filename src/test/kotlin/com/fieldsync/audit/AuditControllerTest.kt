package com.fieldsync.audit

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
class AuditControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    companion object {
        @Container @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")

        @DynamicPropertySource @JvmStatic
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    private fun token(email: String): String {
        val body = """{"email":"$email","password":"password123","orgName":"Audit Org"}"""
        val json = mockMvc.perform(
            post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().isOk).andReturn().response.contentAsString
        return objectMapper.readTree(json).get("token").asText()
    }

    private fun createVisit(token: String, clientId: String) {
        mockMvc.perform(
            post("/api/visits")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"clientId":"$clientId","customerName":"C","visitedAt":"2026-01-01T10:00:00Z"}""")
        ).andExpect(status().isCreated)
    }

    @Test
    fun `records a CREATE entry when a visit is created`() {
        val t = token("audit-create@example.com")
        createVisit(t, "v-1")

        mockMvc.perform(get("/api/audit").header("Authorization", "Bearer $t"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].action").value("CREATE"))
            .andExpect(jsonPath("$.content[0].entityType").value("Visit"))
            .andExpect(jsonPath("$.content[0].occurredAt").exists())
    }

    @Test
    fun `audit log is scoped to the caller's org`() {
        val tokenA = token("audit-a@example.com")
        val tokenB = token("audit-b@example.com")

        createVisit(tokenA, "a-1")   // only org A makes a change

        mockMvc.perform(get("/api/audit").header("Authorization", "Bearer $tokenB"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(0))
    }
}