package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.yaml.snakeyaml.Yaml

class DefaultComponents : ComponentConfigStorage {

    private val componentConfig: Map<String, List<ComponentConfig>> by lazy {
        val configStream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("default-component.yaml")
        val load = Yaml().load<Map<String, Any>>(configStream)
        Jackson.convert(load, jacksonTypeRef())
    }

    override fun getAllComponentConfig(): Map<String, List<ComponentConfig>> {
        return componentConfig
    }
}