package com.fieldsync.visit

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

    @Test
    fun `creates and lists a visit`() {
        val body = """
            {"clientId":"c-1","customerName":"Acme Clinic","visitedAt":"2026-01-01T10:00:00Z"}
        """.trimIndent()

        mockMvc.perform(
            post("/api/visits").contentType(MediaType.APPLICATION_JSON).content(body)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.customerName").value("Acme Clinic"))
            .andExpect(jsonPath("$.id").exists())

        mockMvc.perform(get("/api/visits"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
    }

    @Test
    fun `rejects a payload missing required fields`() {
        mockMvc.perform(
            post("/api/visits").contentType(MediaType.APPLICATION_JSON).content("""{"notes":"x"}""")
        )
            .andExpect(status().isBadRequest)
    }
}
