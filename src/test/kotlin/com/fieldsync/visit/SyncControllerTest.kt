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
import org.springframework.test.web.servlet.ResultActions
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
class SyncControllerTest {

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

    /** Register a fresh org and return a Bearer token. */
    private fun token(email: String): String {
        val body = """{"email":"$email","password":"password123","orgName":"Sync Org"}"""
        val json = mockMvc.perform(
            post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().isOk).andReturn().response.contentAsString
        return objectMapper.readTree(json).get("token").asText()
    }

    private fun sync(token: String, body: String): ResultActions =
        mockMvc.perform(
            post("/api/sync")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )

    @Test
    fun `creates a new record then replays idempotently`() {
        val t = token("sync-create@example.com")
        val body = """{"changes":[{"clientId":"c-1","customerName":"Acme","visitedAt":"2026-01-01T10:00:00Z"}]}"""

        sync(t, body)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.results[0].status").value("APPLIED"))
            .andExpect(jsonPath("$.results[0].version").value(0))

        // Replay the exact same batch: no duplicate, still version 0.
        sync(t, body)
            .andExpect(jsonPath("$.results[0].status").value("APPLIED"))
            .andExpect(jsonPath("$.results[0].version").value(0))

        // Prove only one row exists for this org.
        mockMvc.perform(get("/api/visits").header("Authorization", "Bearer $t"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
    }

    @Test
    fun `updates with a matching base version`() {
        val t = token("sync-update@example.com")
        sync(t, """{"changes":[{"clientId":"c-1","customerName":"V0","visitedAt":"2026-01-01T10:00:00Z"}]}""")
            .andExpect(jsonPath("$.results[0].version").value(0))

        sync(t, """{"changes":[{"clientId":"c-1","baseVersion":0,"customerName":"V1","visitedAt":"2026-01-01T10:00:00Z"}]}""")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.results[0].status").value("APPLIED"))
            .andExpect(jsonPath("$.results[0].version").value(1))
    }

    @Test
    fun `flags a stale base version as a conflict`() {
        val t = token("sync-conflict@example.com")
        sync(t, """{"changes":[{"clientId":"c-1","customerName":"V0","visitedAt":"2026-01-01T10:00:00Z"}]}""")
        sync(t, """{"changes":[{"clientId":"c-1","baseVersion":0,"customerName":"V1","visitedAt":"2026-01-01T10:00:00Z"}]}""")

        // Server is now at version 1; sending baseVersion 0 again is stale.
        sync(t, """{"changes":[{"clientId":"c-1","baseVersion":0,"customerName":"Loser","visitedAt":"2026-01-01T10:00:00Z"}]}""")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.results[0].status").value("CONFLICT"))
            .andExpect(jsonPath("$.results[0].version").value(1))
            .andExpect(jsonPath("$.results[0].server.customerName").value("V1"))
    }

    @Test
    fun `rejects an invalid item without failing the batch`() {
        val t = token("sync-reject@example.com")
        sync(
            t,
            """{"changes":[
                {"clientId":"good","customerName":"Fine","visitedAt":"2026-01-01T10:00:00Z"},
                {"clientId":"bad","customerName":"","visitedAt":"2026-01-01T10:00:00Z"}
            ]}"""
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.results[0].status").value("APPLIED"))
            .andExpect(jsonPath("$.results[1].status").value("REJECTED"))
            .andExpect(jsonPath("$.results[1].reason").exists())
    }
}