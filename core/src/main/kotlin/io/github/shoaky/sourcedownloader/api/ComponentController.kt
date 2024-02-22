package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.component.*
import io.github.shoaky.sourcedownloader.core.componentTypeRef
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.ComponentStateful
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * Component相关接口

 */
@RestController
@RequestMapping("/api/component")
private class ComponentController(
    private val componentManager: ComponentManager,
    private val configOperator: ConfigOperator,
) {

    /**
     * 获取所有Component的信息
     * @param type Component类型
     * @param typeName Component类型名称
     * @param name Component名称
     * @return Component信息列表
     */
    @GetMapping
    fun queryComponents(
        type: ComponentTopType?, typeName: String?, name: String?,
    ): List<ComponentInfo> {
        return componentManager.getAllComponent()
            .filter { name matchesNullOrEqual it.name }
            .filter { type matchesNullOrEqual it.type.topType }
            .filter { typeName matchesNullOrEqual it.type.typeName }
            .map { wrapper ->
                ComponentInfo(
                    wrapper.type.topType,
                    wrapper.type.typeName,
                    wrapper.name,
                    wrapper.props.rawValues,
                    if (wrapper.primary) wrapper.component.let { it as? ComponentStateful }?.stateDetail() else null,
                    wrapper.primary
                )
            }
    }

    /**
     * 创建Component
     * @param type Component类型
     * @param config Component配置
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createComponent(@RequestBody body: ComponentCreateBody) {
        configOperator.save(
            body.type.lowerHyphenName(),
            ComponentConfig(
                body.name,
                body.typeName,
                body.props
            )
        )
    }

    /**
     * 删除Component
     * @param type Component类型
     * @param typeName Component类型名称
     * @param name Component名称
     */
    @DeleteMapping("/{type}/{typeName}/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteComponent(
        @PathVariable type: ComponentTopType,
        @PathVariable typeName: String,
        @PathVariable name: String,
    ) {
        val componentType = ComponentType.of(type, typeName)
        val component = componentManager.getComponent(componentType, name)
        if (component?.getRefs()?.isNotEmpty() == true) {
            throw ComponentException.other("Component is referenced by ${component.getRefs()} processors")
        }

        configOperator.deleteComponent(type, typeName, name)
        // 待定是否要删除
        componentManager.destroy(componentType, name)
    }

    /**
     * 获取Component描述(未完成)
     */
    @GetMapping("/descriptions")
    fun getComponentDescriptions(): List<ComponentDescription> {
        return componentManager.getComponentDescriptions()
            .sortedBy { description ->
                description.types.maxOfOrNull { it.topType }
            }
    }

    /**
     * 重载Component
     * @param type Component类型
     * @param typeName Component类型名称
     * @param name Component名称
     */
    @GetMapping("/{type}/{typeName}/{name}/reload")
    fun reload(
        @PathVariable type: ComponentTopType,
        @PathVariable typeName: String,
        @PathVariable name: String,
    ) {
        val componentType = ComponentType.of(type, typeName)
        componentManager.destroy(componentType, name)
        componentManager.getComponent(type, ComponentId("$typeName:$name"), componentTypeRef())
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
    // val detail: ComponentDetail,
    val stateDetail: Any? = null,
    val primary: Boolean
)

private data class ComponentCreateBody(
    val type: ComponentTopType,
    val typeName: String,
    val name: String,
    val props: Map<String, Any>,
)

private infix fun <T> T?.matchesNullOrEqual(any: T?): Boolean {
    if (any == null) {
        return true
    }
    return this == any
}