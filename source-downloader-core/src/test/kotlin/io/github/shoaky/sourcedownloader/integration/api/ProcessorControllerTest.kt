package io.github.shoaky.sourcedownloader.integration.api

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class ProcessorControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun get_processors() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/processor"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect {
                MockMvcResultMatchers.jsonPath("$.*").isNotEmpty
            }
    }

    @Test
    fun get_processor_config() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/processor/NormalCase"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect {
                MockMvcResultMatchers.jsonPath("$.name").value("NormalCase")
            }
    }

    @Test
    @Disabled("增加MockSource后开启,不然创建文件比较麻烦")
    fun dry_run() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/processor/dry-run/NormalCase"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect {
                MockMvcResultMatchers.jsonPath("$.*").isNotEmpty
            }
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun clean() {
            Path("test.db").deleteIfExists()
        }
    }
}