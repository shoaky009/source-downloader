package xyz.shoaky.sourcedownloader.core

// import io.swagger.v3.oas.annotations.media.Schema
// import io.swagger.v3.oas.annotations.media.SchemaProperty
import com.fasterxml.jackson.annotation.JsonValue
import xyz.shoaky.sourcedownloader.core.file.ParsingFailedStrategy
import xyz.shoaky.sourcedownloader.core.file.RenameMode
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.component.*
import java.nio.file.Path
import java.time.Duration
import kotlin.reflect.KClass

data class ProcessorConfig(
    // @Schema(description = "处理器名称，定义后最好不要更改，也不要出现和其他处理器名字冲突", required = true)
    val name: String,
    // @Schema(description = "触发器支持多种触发方式", required = true, type = "ComponentId")
    val triggers: List<ComponentId>,
    // @Schema(description = "源数据转换为SourceItem的组件", required = true, type = "ComponentId")
    val source: ComponentId,
    // @Schema(description = "变量名称提供者，根据SourceItem中的信息提供变量", required = true, type = "ComponentId")
    val providers: List<ComponentId> = emptyList(),
    val downloader: ComponentId = ComponentId("downloader:url"),
    val mover: ComponentId = ComponentId("mover:general"),
    val savePath: Path,
    val sourceItemFilters: List<ComponentId> = emptyList(),
    val sourceFileFilters: List<ComponentId> = emptyList(),
    val options: Options = Options(),
) {
    fun getSourceInstanceName(): String {
        return source.getInstanceName(Source::class)
    }

    fun getProviderInstanceNames(): List<String> {
        return providers.map {
            it.getInstanceName(VariableProvider::class)
        }
    }

    fun getDownloaderInstanceName(): String {
        return downloader.getInstanceName(Downloader::class)
    }

    fun getMoverInstanceName(): String {
        return mover.getInstanceName(FileMover::class)
    }

    fun getTriggerInstanceNames(): List<String> {
        return triggers.map {
            it.getInstanceName(Trigger::class)
        }
    }

    data class Options(
        val fileSavePathPattern: PathPattern = PathPattern.ORIGIN,
        val filenamePattern: PathPattern = PathPattern.ORIGIN,
        val runAfterCompletion: List<ComponentId> = emptyList(),
        val renameTaskInterval: Duration = Duration.ofMinutes(5),
        val downloadCategory: String? = null,
        // NOT IMPLEMENTED
        val variableConflictDecision: VariableConflictDecision = VariableConflictDecision.SMART,
        // 修改文件夹创建时间
        val touchItemDirectory: Boolean = true,
        val renameTimesThreshold: Int = 3,
        val provideMetadataVariables: Boolean = true,
        val saveContent: Boolean = true,
        val renameMode: RenameMode = RenameMode.MOVE,
        val itemExpressionExclusions: List<String> = emptyList(),
        val itemExpressionInclusions: List<String> = emptyList(),
        val fileExpressionExclusions: List<String> = emptyList(),
        val fileExpressionInclusions: List<String> = emptyList(),
        val parsingFailedStrategy: ParsingFailedStrategy = ParsingFailedStrategy.USE_ORIGINAL_FILENAME
    )

    enum class VariableConflictDecision {
        ANY,
        VOTE,
        ACCURACY,

        /**
         * VOTE + ACCURACY
         */
        SMART
    }

    data class ComponentId(
        @JsonValue
        val id: String,
    ) {
        fun <T : SdComponent> getInstanceName(klass: KClass<T>): String {
            val split = id.split(":")
            return getComponentType(klass).instanceName(split.last())
        }

        fun <T : SdComponent> getComponentType(klass: KClass<T>): ComponentType {
            val split = id.split(":")
            if (split.isEmpty()) {
                throw RuntimeException("重命名器配置错误:${id}")
            }
            return ComponentType(split.first(), klass)
        }
    }
}


