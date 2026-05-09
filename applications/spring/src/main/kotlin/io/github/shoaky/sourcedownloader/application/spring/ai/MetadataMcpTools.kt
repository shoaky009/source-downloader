package io.github.shoaky.sourcedownloader.application.spring.ai

import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.service.ConfigAssistantApplyRequest
import io.github.shoaky.sourcedownloader.service.ConfigAssistantApplyResponse
import io.github.shoaky.sourcedownloader.service.ConfigAssistantDraft
import io.github.shoaky.sourcedownloader.service.ConfigAssistantDraftRequest
import io.github.shoaky.sourcedownloader.service.ConfigAssistantDraftResponse
import io.github.shoaky.sourcedownloader.service.ConfigAssistantService
import io.github.shoaky.sourcedownloader.service.ComponentInfo
import io.github.shoaky.sourcedownloader.service.ComponentService
import io.github.shoaky.sourcedownloader.service.MetadataService
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Component

@Component
class MetadataMcpTools(
    private val metadataService: MetadataService,
    private val configAssistantService: ConfigAssistantService,
    private val componentService: ComponentService,
) {

    @Tool(name = "get_processor_metadata", description = "获取 processor 配置元数据")
    fun getProcessorMetadata() = metadataService.processorMetadata()

    @Tool(name = "get_component_root_types", description = "获取组件大类元数据")
    fun getComponentRootTypes() = metadataService.componentRootTypes()

    @Tool(name = "list_component_types", description = "获取所有组件类型的轻量摘要列表")
    fun listComponentTypes() = metadataService.componentCapabilitySummaries()

    @Tool(name = "list_instance_types", description = "获取所有 instance 类型的轻量摘要列表")
    fun listInstanceTypes() = metadataService.instanceCapabilitySummaries()

    @Tool(name = "list_existing_components", description = "查询当前系统里已经存在的组件，供模型优先复用")
    fun listExistingComponents(
        @ToolParam(description = "组件大类，例如 source、downloader；不传表示不过滤", required = false) rootType: String? = null,
        @ToolParam(description = "组件类型名，例如 telegram、rss；不传表示不过滤", required = false) typeName: String? = null,
        @ToolParam(description = "组件名称；不传表示不过滤", required = false) name: String? = null,
    ): List<ComponentInfo> {
        return componentService.queryComponents(
            type = rootType?.let {
                ComponentRootType.fromName(it) ?: throw IllegalArgumentException("Unknown rootType: $rootType")
            },
            typeName = typeName,
            name = name,
        )
    }

    @Tool(name = "get_component_type_detail", description = "获取指定组件类型的详细 metadata 和 rules")
    fun getComponentTypeDetail(
        @ToolParam(description = "组件大类，例如 source、downloader", required = true) rootType: String,
        @ToolParam(description = "组件类型名，例如 telegram、rss", required = true) typeName: String,
    ) = metadataService.componentCapability(
        ComponentRootType.fromName(rootType) ?: throw IllegalArgumentException("Unknown rootType: $rootType"),
        typeName,
    )

    @Tool(name = "get_instance_type_detail", description = "获取指定 instance 类型的详细 metadata")
    fun getInstanceTypeDetail(
        @ToolParam(description = "instance 类型值，例如 telegram4j.core.MTProtoTelegramClient", required = true) type: String,
    ) = metadataService.instanceCapability(type)

    @Tool(name = "draft_config", description = "根据当前输入、已有回答和上一轮草稿推进配置草稿；模型应优先自己补全可确定字段，只在无法确定时再把 question 返回给用户")
    fun draftConfig(
        @ToolParam(description = "用户自然语言需求", required = true) input: String,
        @ToolParam(description = "当前轮已经确认的回答，key 为字段路径", required = false) answers: Map<String, Any> = emptyMap(),
        @ToolParam(description = "上一轮已有配置草稿中的 draft 字段", required = false) draft: ConfigAssistantDraft? = null,
    ): ConfigAssistantDraftResponse {
        return configAssistantService.draft(
            ConfigAssistantDraftRequest(
                input = input,
                answers = answers,
                draft = draft,
            )
        )
    }

    @Tool(name = "apply_draft", description = "应用已经完整的配置草稿")
    fun applyDraft(
        @ToolParam(description = "完整的草稿对象", required = true) request: ConfigAssistantApplyRequest,
    ): ConfigAssistantApplyResponse {
        return configAssistantService.apply(request)
    }
}
