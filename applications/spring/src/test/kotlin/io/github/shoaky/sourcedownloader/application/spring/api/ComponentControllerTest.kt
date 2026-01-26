package io.github.shoaky.sourcedownloader.application.spring.api

import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class ComponentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var componentManager: ComponentManager

    @Autowired
    private lateinit var configOperator: ConfigOperator

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
        val string =
            """{"type":"source","name":"api-create","typeName":"system-file","props":{"path":"src/test/resources/sources"}}"""
        mockMvc.perform(
            post("/api/component")
                .content(string.toByteArray())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is2xxSuccessful)
            .andExpect {
                configOperator.getComponentConfig(ComponentRootType.SOURCE, "system-file", "api-create")
            }
    }

    @Test
    fun delete_component() {
        mockMvc.perform(
            delete("/api/component/source/system-file/api-create2")
                .content(
                    """{"name":"api-create2","type":"system-file","props":{"path":"src/test/resources/sources"}}"""
                ).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is2xxSuccessful)
            .andExpect {
                val component = componentManager.getComponent(
                    ComponentType(ComponentRootType.SOURCE, "system-file"),
                    "api-create2"
                )
                component != null
            }

        mockMvc.perform(
            delete("/api/component/source/system-file/api-create2")
        ).andExpect(status().is2xxSuccessful)
            .andExpect {
                val component = componentManager.getComponent(
                    ComponentType(ComponentRootType.SOURCE, "system-file"),
                    "api-create2"
                )
                component != null
            }
    }

    @Test
    fun delete_ref_component() {
        mockMvc.perform(
            delete("/api/component/source/fixed/error-item")
        ).andExpect(status().is4xxClientError)
    }

}