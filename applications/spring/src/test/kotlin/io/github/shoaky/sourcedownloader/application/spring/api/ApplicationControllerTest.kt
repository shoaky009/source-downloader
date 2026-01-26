package io.github.shoaky.sourcedownloader.application.spring.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class ApplicationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun reload() {
        mockMvc.perform(post("/api/application/reload"))
            .andExpect(status().is2xxSuccessful)
    }

}