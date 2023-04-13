package xyz.shoaky.sourcedownloader.component.supplier

import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.component.MultipleSource
import xyz.shoaky.sourcedownloader.core.SdComponentManager
import xyz.shoaky.sourcedownloader.core.config.ProcessorConfig
import xyz.shoaky.sourcedownloader.sdk.component.*

@Component
class MultipleSourceSupplier(val componentManager: SdComponentManager) : SdComponentSupplier<MultipleSource> {
    override fun apply(props: ComponentProps): MultipleSource {
        val sources = props.getOrDefault("sources", emptyList<String>())
            .map { ProcessorConfig.ComponentId(it) }
            .map {
                val instanceName = it.getInstanceName(Source::class)
                val source = componentManager.getComponent(instanceName) as? Source
                    ?: throw ComponentException.missing("Source $instanceName not found")
                source
            }.toTypedArray()
        return MultipleSource(*sources)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("multiple"),
            ComponentType.source("aggregated"),
        )
    }

    override fun getComponentClass(): Class<MultipleSource> {
        return MultipleSource::class.java
    }
}