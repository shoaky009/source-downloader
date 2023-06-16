package io.github.shoaky.sourcedownloader.integration.api

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class ProcessingContentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    companion object {
        @JvmStatic
        @AfterAll
        fun clean() {
            Path("test.db").deleteIfExists()
        }
    }
}