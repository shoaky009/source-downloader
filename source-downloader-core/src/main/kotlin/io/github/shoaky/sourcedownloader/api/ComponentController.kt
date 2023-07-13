package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.core.component.*
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/component")
private class ComponentController(
    private val componentManager: ComponentManager,
    private val configOperator: ConfigOperator,
    private val componentService: ComponentService
) {

    @GetMapping
    fun getComponents(
        type: ComponentTopType?, typeName: String?, name: String?,
    ): List<ComponentInfo> {
        return componentManager.getAllComponent()
            .filter { name == null || it.name == name }
            .filter { type == null || it.type.topType == type }
            .filter { typeName == null || it.type.typeName == typeName }
            .map {
                ComponentInfo(
                    it.type.topType,
                    it.type.typeName,
                    it.name,
                    it.props.rawValues,
                    ComponentDetail(),
                    it.component as? ComponentStateful,
                    it.primary
                )
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
        componentManager.createComponent(componentType, name, props)
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
    ): ResponseEntity<Any> {
        // TODO 聚合componentManager和processorManager的业务层,如果有Processor引用组件不允许删除
        // check if processor is using this component
        // componentManager.destroyComponent(name, componentType)
        val deleted = configOperator.deleteComponent(type, typeName, name)
        return if (deleted) ResponseEntity.ok().build() else ResponseEntity.noContent().build()
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
    val primary: Boolean
)

@Service
private class ComponentService(
    private val componentManager: ComponentManager,
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