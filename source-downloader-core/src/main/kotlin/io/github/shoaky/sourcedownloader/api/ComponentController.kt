package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.core.component.ComponentConfig
import io.github.shoaky.sourcedownloader.core.component.ComponentConfigStorage
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import io.github.shoaky.sourcedownloader.core.file.ComponentDescription
import io.github.shoaky.sourcedownloader.core.file.SdComponentManager
import io.github.shoaky.sourcedownloader.sdk.ComponentStateful
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.*
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/component")
private class ComponentController(
    private val componentManager: SdComponentManager,
    private val configOperator: ConfigOperator,
    private val componentService: ComponentService
) {

    @GetMapping
    fun getComponents(
        type: ComponentTopType?, typeName: String?, name: String?,
    ): List<ComponentInfo> {
        val componentConfigs = componentService.getComponentConfigs()
        return componentConfigs
            .filter {
                type == null || it.key == type
            }
            .flatMap { entry ->
                val componentName = entry.key
                entry.value
                    .filter {
                        typeName == null || it.type == typeName
                    }.filter {
                        name == null || it.name == name
                    }.map {
                        val instanceName = ComponentType(it.type, componentName).instanceName(it.name)
                        val stateful = componentManager.getComponent(instanceName) as? ComponentStateful
                        ComponentInfo(
                            componentName,
                            it.type,
                            it.name,
                            it.props,
                            ComponentDetail(),
                            stateful?.stateDetail()
                        )
                    }
            }
    }

    @PostMapping("/{type}/{typeName}/{name}")
    @ResponseStatus(HttpStatus.CREATED)
    fun createComponent(
        @PathVariable type: ComponentTopType,
        @PathVariable typeName: String,
        @PathVariable name: String,
        @RequestBody body: Map<String, Any>
    ) {
        val props = Properties.fromMap(body)
        val componentType = ComponentType(typeName, type.klass)
        componentManager.createComponent(name, componentType, props)
        configOperator.save(
            type.lowerHyphenName(),
            ComponentConfig(name, typeName, body)
        )
    }

    @DeleteMapping("/{type}/{typeName}/{name}")
    fun deleteComponent(
        @PathVariable type: ComponentTopType,
        @PathVariable typeName: String,
        @PathVariable name: String,
    ) {
        // TODO 聚合componentManager和processorManager的业务层,如果有Processor引用组件不允许删除
        // check if processor is using this component
        // componentManager.destroyComponent(name, componentType)
        configOperator.deleteComponent(type, typeName, name)
    }

    @GetMapping("/descriptions")
    fun getComponentDescriptions(): List<ComponentDescription> {
        return componentManager.getComponentDescriptions()
            .sortedBy { cd ->
                cd.types.maxOfOrNull { it.topType }
            }
    }
}

private data class ComponentDetail(
    val description: String? = null,
    val variables: List<String> = emptyList(),
    val rules: List<String> = emptyList()
)

private data class ComponentInfo(
    val type: ComponentTopType,
    val typeName: String,
    val name: String,
    val props: Map<String, Any>,
    val detail: ComponentDetail,
    val stateDetail: Any? = null,
)

@Service
private class ComponentService(
    private val componentManager: SdComponentManager,
    private val configStorages: List<ComponentConfigStorage>
) {
    fun getComponentConfigs(): Map<ComponentTopType, List<ComponentConfig>> {
        val map = mutableMapOf<ComponentTopType, MutableList<ComponentConfig>>()
        for (cc in configStorages) {
            val allComponents = cc.getAllComponentConfig()
            for (allComponent in allComponents) {
                val components = ComponentTopType.fromName(allComponent.key)
                    ?: throw ComponentException.unsupported("Unknown component type: ${allComponent.key}")
                map.getOrPut(components) {
                    mutableListOf()
                }.addAll(allComponent.value)
            }
        }
        return map
    }
}