package io.github.shoaky.sourcedownloader.integration.api

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists

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

    companion object {
        @JvmStatic
        @AfterAll
        fun clean() {
            Path("test.db").deleteIfExists()
        }
    }
}