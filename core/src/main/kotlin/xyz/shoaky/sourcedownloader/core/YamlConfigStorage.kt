package xyz.shoaky.sourcedownloader.core

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.ResolvableType
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.core.config.ComponentConfig
import xyz.shoaky.sourcedownloader.core.config.ProcessorConfigs

@Component
@EnableConfigurationProperties(ProcessorConfigs::class)
class YamlConfigStorage(
    private val configs: ProcessorConfigs,
    private val environment: Environment
) : ProcessorConfigStorage, ComponentConfigStorage {
    override fun getAllProcessor(): List<ProcessorConfig> {
        return configs.processors
    }

    override fun getAllComponents(): Map<String, List<ComponentConfig>> {
        val bindType = Bindable.of<Map<String, List<ComponentConfig>>>(
            ResolvableType.forType(object : ParameterizedTypeReference<Map<String, List<ComponentConfig>>>() {})
        )
        val componentConfigBinder = Binder.get(environment)
            .bind("components", bindType)

        if (componentConfigBinder.isBound.not()) {
            log.info("没有组件配置在yaml中")
            return emptyMap()
        }
        return componentConfigBinder.get()
    }
}