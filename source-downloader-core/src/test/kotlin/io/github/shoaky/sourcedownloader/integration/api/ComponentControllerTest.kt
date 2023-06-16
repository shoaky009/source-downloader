package io.github.shoaky.sourcedownloader.integration.api

import io.github.shoaky.sourcedownloader.core.file.SdComponentManager
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class ComponentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var componentManager: SdComponentManager

    @Test
    fun get_components() {
        mockMvc.perform(get("/api/component"))
            .andExpect(status().isOk)
            .andExpect {
                MockMvcResultMatchers.jsonPath("$.*").isNotEmpty
            }
    }

    @Test
    fun create_component() {
        mockMvc.perform(
            post("/api/component/source/system-file/api-create")
                .content(
                    """{"path": "src/test/resources/sources"}"""
                ).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is2xxSuccessful)
            .andExpect {
                val instanceName = ComponentType("system-file", ComponentTopType.SOURCE).instanceName("api-create")
                val component = componentManager.getComponent(instanceName)
                component != null
            }
    }

    @Test
    fun delete_component() {
        mockMvc.perform(
            post("/api/component/source/system-file/api-create2")
                .content(
                    """{"path": "src/test/resources/sources"}"""
                ).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is2xxSuccessful)
            .andExpect {
                val instanceName = ComponentType("system-file", ComponentTopType.SOURCE).instanceName("api-create2")
                val component = componentManager.getComponent(instanceName)
                component != null
            }

        mockMvc.perform(
            delete("/api/component/source/system-file/api-create2")
        ).andExpect(status().isOk)
            .andExpect {
                val instanceName = ComponentType("system-file", ComponentTopType.SOURCE).instanceName("api-create2")
                val component = componentManager.getComponent(instanceName)
                component != null
            }
    }

    @Test
    fun get_component_desc() {
        mockMvc.perform(get("/api/component/descriptions"))
            .andExpect(status().isOk)
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