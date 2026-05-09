package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.DefaultInstanceManager
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.sdk.util.Jackson

class MetadataService(
    private val componentManager: ComponentManager,
    private val instanceManager: InstanceManager,
) {

    private val componentCapabilities: List<ComponentCapabilityDetail> by lazy {
        componentManager.getSuppliers().map { supplier ->
            ComponentCapabilityDetail(
                supportNoArgs = supplier.supportNoArgs(),
                types = supplier.supplyTypes().map {
                    ComponentCapabilityType(
                        rootType = it.type,
                        typeName = it.typeName,
                        fullName = it.fullName(),
                    )
                },
                description = supplier.metadata()?.description,
                metadata = supplier.metadata(),
                rules = supplier.rules().map {
                    ComponentCapabilityRule(
                        isAllow = it.isAllow,
                        rootType = it.type,
                        componentClassName = it.value.qualifiedName ?: it.value.simpleName.orEmpty(),
                    )
                }
            )
        }.sortedBy { it.types.firstOrNull()?.fullName ?: "" }
    }

    private val processorMetadata: ProcessorMetadata by lazy {
        val configStream = loadResource(PROCESSOR_METADATA_RESOURCE)
        Jackson.fromJson(configStream, ProcessorMetadata::class)
    }

    private val instanceCapabilities: List<InstanceCapabilityDetail> by lazy {
        val manager = instanceManager as? DefaultInstanceManager ?: return@lazy emptyList()
        manager.getInstanceFactories().map { factory ->
            InstanceCapabilityDetail(
                type = factory.type().name,
                simpleName = factory.type().simpleName,
                description = factory.metadata()?.description,
                metadata = factory.metadata(),
            )
        }
    }

    fun processorMetadata(): ProcessorMetadata {
        return processorMetadata
    }

    fun componentRootTypes(): List<ComponentRootTypeMetadata> {
        return ComponentRootType.entries.map {
            ComponentRootTypeMetadata(
                rootType = it,
                primaryName = it.primaryName,
                aliases = it.alias,
                componentInterface = it.klass.qualifiedName ?: it.klass.simpleName.orEmpty(),
                description = rootTypeDescription(it),
            )
        }
    }

    fun componentCapabilitySummaries(): List<ComponentCapabilitySummary> {
        return componentCapabilities.map {
            ComponentCapabilitySummary(
                supportNoArgs = it.supportNoArgs,
                types = it.types,
                description = it.description,
            )
        }
    }

    fun componentCapability(rootType: ComponentRootType, typeName: String): ComponentCapabilityDetail? {
        return componentCapabilities.firstOrNull {
            it.types.any { type -> type.rootType == rootType && type.typeName == typeName }
        }
    }

    fun componentCapabilities(): List<ComponentCapabilityDetail> {
        return componentCapabilities
    }

    fun instanceCapabilitySummaries(): List<InstanceCapabilitySummary> {
        return instanceCapabilities.map {
            InstanceCapabilitySummary(
                type = it.type,
                simpleName = it.simpleName,
                description = it.description,
            )
        }
    }

    fun instanceCapability(type: String): InstanceCapabilityDetail? {
        return instanceCapabilities.firstOrNull { it.type == type }
    }

    private companion object {

        const val PROCESSOR_METADATA_RESOURCE = "metadata/processor-metadata.json"
    }

    private fun rootTypeDescription(rootType: ComponentRootType): String {
        return when (rootType) {
            ComponentRootType.TRIGGER -> "负责触发 processor 执行的组件，例如 cron、fixed、webhook。"
            ComponentRootType.SOURCE -> "负责从外部系统拉取或遍历条目数据的组件。"
            ComponentRootType.DOWNLOADER -> "负责下载、导入或提交文件任务的组件。"
            ComponentRootType.ITEM_FILE_RESOLVER -> "负责把 source item 解析成一个或多个 SourceFile 的组件。"
            ComponentRootType.FILE_MOVER -> "负责把下载结果从 downloadPath 落到 savePath 的组件。"
            ComponentRootType.VARIABLE_PROVIDER -> "负责为命名模板和规则提供变量的组件。"
            ComponentRootType.PROCESS_LISTENER -> "负责在处理过程中或处理后执行附加动作的监听组件。"
            ComponentRootType.SOURCE_ITEM_FILTER -> "负责按 item 维度筛选条目的组件。"
            ComponentRootType.SOURCE_FILE_FILTER -> "负责按 source file 维度筛选文件的组件。"
            ComponentRootType.ITEM_CONTENT_FILTER -> "负责按 item content 维度过滤处理结果的组件。"
            ComponentRootType.FILE_CONTENT_FILTER -> "负责按 file content 维度过滤文件结果的组件。"
            ComponentRootType.TAGGER -> "负责为文件内容打标签的组件。"
            ComponentRootType.FILE_REPLACEMENT_DECIDER -> "负责目标文件已存在时，决定是否允许替换的组件。"
            ComponentRootType.FILE_EXISTS_DETECTOR -> "负责补充检测目标文件是否已存在的组件。"
            ComponentRootType.VARIABLE_REPLACER -> "负责对命名变量进行替换和清洗的组件。"
            ComponentRootType.TRIMMER -> "负责对变量值进行裁剪、去噪或规整的组件。"
            ComponentRootType.MANUAL_SOURCE -> "支持手动提交 item 的 source 组件。"
        }
    }

    private fun loadResource(path: String) = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream(path)
        ?: throw IllegalStateException("Resource $path not found")
}
