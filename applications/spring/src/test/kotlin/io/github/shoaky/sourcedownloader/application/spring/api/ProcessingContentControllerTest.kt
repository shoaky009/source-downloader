package io.github.shoaky.sourcedownloader.application.spring.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class ProcessingContentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

}