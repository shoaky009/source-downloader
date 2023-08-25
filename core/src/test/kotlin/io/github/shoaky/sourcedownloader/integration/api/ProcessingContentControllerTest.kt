package io.github.shoaky.sourcedownloader.integration.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class ProcessingContentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

}