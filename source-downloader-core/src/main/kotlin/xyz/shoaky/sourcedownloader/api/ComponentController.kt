package xyz.shoaky.sourcedownloader.api

import org.springframework.web.bind.annotation.*
import xyz.shoaky.sourcedownloader.core.ComponentConfigStorage
import xyz.shoaky.sourcedownloader.core.ConfigOperator
import xyz.shoaky.sourcedownloader.core.SdComponentManager
import xyz.shoaky.sourcedownloader.core.config.ComponentConfig
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.Components

@RestController
@RequestMapping("/api/component")
private class ComponentController(
    private val componentManager: SdComponentManager,
    private val configOperator: ConfigOperator,
    private val ccs: List<ComponentConfigStorage>
) {

    @GetMapping
    fun getComponents(): Any {
        val map = mapOf<String, List<ComponentConfig>>()
        for (cc in ccs) {
            val allComponents = cc.getAllComponentConfig()
        }
        return map
    }

    @PostMapping("/{type}/{typeName}/{name}")
    fun createComponent(@PathVariable type: Components,
                        @PathVariable typeName: String,
                        @PathVariable name: String,
                        @RequestBody body: Map<String, Any>) {
        val props = ComponentProps.fromMap(body)
        val componentType = ComponentType(typeName, type.klass)
        componentManager.createComponent(name, componentType, props)
        configOperator.save(
            type.lowerHyphenName(),
            ComponentConfig(name, typeName, body)
        )
    }

    @DeleteMapping("/{type}/{typeName}/{name}")
    fun deleteComponent(
        @PathVariable type: Components,
        @PathVariable typeName: String,
        @PathVariable name: String,
    ) {
        // TODO 聚合componentManager和processorManager的业务层,如果有Processor引用组件不允许删除
        val componentType = ComponentType(typeName, type.klass)
        // componentManager.destroyComponent(name, componentType)
        configOperator.delete(
            type.lowerHyphenName(),
            ComponentConfig(name, typeName, emptyMap())
        )
    }

    @GetMapping("/types")
    fun getComponentTypes(): Map<String, List<String>> {
        return componentManager.getSuppliers()
            .flatMap { it.supplyTypes() }
            .map {
                it.klass.simpleName!! to it.typeName
            }.groupBy({ it.first }, { it.second })
    }

}