package io.github.shoaky.sourcedownloader.application.spring.api

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.TypeRef
import com.jayway.jsonpath.spi.json.JacksonJsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.nio.file.Path
import kotlin.io.path.notExists

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class ProcessorControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var processingStorage: ProcessingStorage

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

    /**
     * 接口需要返回处理数据
     * 不能有真实的下载动作
     * 不能持久化处理数据
     */
    @Test
    fun dry_run() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/processor/DryRunCase/dry-run"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { mockRes ->
                MockMvcResultMatchers.jsonPath("$.*").isNotEmpty

                val config = Configuration.builder()
                    .jsonProvider(JacksonJsonProvider())
                    .mappingProvider(JacksonMappingProvider())
                    .build()
                val read = JsonPath.parse(mockRes.response.contentAsString, config).read("$.*.fileResults[*].from", object : TypeRef<List<Path>>() {})
                assert(read.all { it.notExists() })
            }
        runCatching {
            processingStorage.queryAllContent(ProcessingQuery("DryRunCase"))
        }.onSuccess {
            assert(it.isEmpty())
        }.onFailure {
            println(it)
        }
    }

}