package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml

@Component
class DefaultComponents : ComponentConfigStorage {
    override fun getAllComponentConfig(): Map<String, List<ComponentConfig>> {
        val config = ClassPathResource("default-component.yaml")
        val load = Yaml().load<Map<String, Any>>(config.inputStream)
        return Jackson.convert(load, jacksonTypeRef())
    }
}