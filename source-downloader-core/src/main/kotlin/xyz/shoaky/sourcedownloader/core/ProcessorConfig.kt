package xyz.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonValue
import xyz.shoaky.sourcedownloader.core.idk.ParsingFailedStrategy
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.component.*
import java.nio.file.Path
import java.time.Duration
import kotlin.reflect.KClass

data class ProcessorConfig(
    val name: String,
    val trigger: ComponentId,
    val source: ComponentId,
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

    fun getTriggerInstanceName(): String {
        return trigger.getInstanceName(Trigger::class)
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
        val parsingFailsUsingTheOriginal: Boolean = true,
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


