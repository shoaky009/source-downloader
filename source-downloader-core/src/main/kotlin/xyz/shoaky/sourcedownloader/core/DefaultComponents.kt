package xyz.shoaky.sourcedownloader.core

import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml
import xyz.shoaky.sourcedownloader.core.config.ComponentConfig
import xyz.shoaky.sourcedownloader.sdk.util.Jackson

@Component
class DefaultComponents : ComponentConfigStorage {
    override fun getAllComponents(): Map<String, List<ComponentConfig>> {
        val yaml = ClassPathResource("default-component.yaml")
        val load = Yaml().load<Map<Any, Any>>(yaml.inputStream)
        return Jackson.convert(load, object : TypeReference<Map<String, List<ComponentConfig>>>() {})
    }
}