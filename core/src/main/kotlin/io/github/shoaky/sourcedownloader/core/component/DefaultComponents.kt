package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml

@Component
class DefaultComponents : ComponentConfigStorage {

    private val componentConfig: Map<String, List<ComponentConfig>> by lazy {
        val config = ClassPathResource("default-component.yaml")
        val load = Yaml().load<Map<String, Any>>(config.inputStream)
        Jackson.convert(load, jacksonTypeRef())
    }

    override fun getAllComponentConfig(): Map<String, List<ComponentConfig>> {
        return componentConfig
    }
}