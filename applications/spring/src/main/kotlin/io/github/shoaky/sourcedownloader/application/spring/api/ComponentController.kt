package io.github.shoaky.sourcedownloader.application.spring.api

import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.service.ComponentCreateBody
import io.github.shoaky.sourcedownloader.service.ComponentInfo
import io.github.shoaky.sourcedownloader.service.ComponentService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*

/**
 * Component相关接口
 */
@RestController
@RequestMapping("/api/component")
private class ComponentController(
    private val componentService: ComponentService
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
        return componentService.queryComponents(type, typeName, name)
    }

    /**
     * 创建Component
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createComponent(@RequestBody body: ComponentCreateBody) {
        componentService.createComponent(body)
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
        componentService.deleteComponent(type, typeName, name)
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
        componentService.reload(type, typeName, name)
    }

    /**
     * @param id Component实例名称例如`Downloader:telegram:telegram`
     */
    @GetMapping("/state-stream")
    suspend fun stateDetailStream(
        @RequestParam id: MutableSet<String>,
    ): Flow<ServerSentEvent<Any>> {
        return componentService.stateDetailStream(id).map {
            ServerSentEvent.builder(it.data).event(it.event).id(it.id).build()
        }
    }

    @GetMapping("/types")
    fun getTypes(type: ComponentTopType?): List<ComponentType> {
        return componentService.getTypes(type)
    }

    @GetMapping("/{type}/{typeName}/metadata")
    fun getSchema(
        @PathVariable type: ComponentTopType,
        @PathVariable typeName: String
    ): ComponentMetadata? {
        return componentService.getSchema(type, typeName)
    }
}