package io.github.shoaky.sourcedownloader.integration.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class ApplicationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun reload() {
        mockMvc.perform(get("/api/application/reload"))
            .andExpect(status().isOk)
    }

    @Test
    fun info() {
        mockMvc.perform(get("/api/application/info"))
            .andExpect(status().isOk)
    }
}