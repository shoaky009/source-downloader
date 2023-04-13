package xyz.shoaky.sourcedownloader.core

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml
import xyz.shoaky.sourcedownloader.core.config.ComponentConfig
import xyz.shoaky.sourcedownloader.sdk.util.Jackson

@Component
class DefaultComponents : ComponentConfigStorage {
    override fun getAllComponentConfig(): Map<String, List<ComponentConfig>> {
        val config = ClassPathResource("default-component.yaml")
        val load = Yaml().load<Map<String, Any>>(config.inputStream)
        return Jackson.convert(load, jacksonTypeRef())
    }
}