package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.core.component.*
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/component")
private class ComponentController(
    private val componentManager: ComponentManager,
    private val configOperator: ConfigOperator,
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

    @DeleteMapping("/{topType}/{typeName}/{name}")
    fun deleteComponent(
        @PathVariable topType: ComponentTopType,
        @PathVariable typeName: String,
        @PathVariable name: String,
    ): ResponseEntity<Any> {
        val componentType = ComponentType.of(topType, typeName)
        val component = componentManager.getComponent(componentType, name)
        if (component?.getRefs()?.isNotEmpty() == true) {
            throw ComponentException.other("Component is referenced by ${component.getRefs()} processors")
        }

        configOperator.deleteComponent(topType, typeName, name)
        componentManager.destroy(componentType, name)
        return ResponseEntity.ok().build()
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