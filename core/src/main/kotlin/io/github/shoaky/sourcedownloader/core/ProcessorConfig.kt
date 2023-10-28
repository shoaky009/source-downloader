package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import io.github.shoaky.sourcedownloader.core.component.ComponentId
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import io.github.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import io.github.shoaky.sourcedownloader.core.processor.ListenerMode
import io.github.shoaky.sourcedownloader.core.processor.VariableConflictStrategy
import io.github.shoaky.sourcedownloader.sdk.DownloadOptions
import io.github.shoaky.sourcedownloader.sdk.PathPattern
import java.nio.file.Path
import java.time.Duration

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class ProcessorConfig(
    val name: String,
    val triggers: List<ComponentId> = emptyList(),
    val source: ComponentId,
    @JsonAlias("file-resolver")
    val itemFileResolver: ComponentId,
    val downloader: ComponentId,
    @JsonAlias("mover")
    val fileMover: ComponentId = ComponentId("mover:general"),
    @JsonSerialize(using = ToStringSerializer::class)
    val savePath: Path,
    val options: Options = Options(),
    val enabled: Boolean = true
) {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    data class Options(
        @JsonAlias("variable-providers")
        val variableProviders: List<ComponentId> = emptyList(),
        @JsonAlias("item-filters")
        val sourceItemFilters: List<ComponentId> = emptyList(),
        @JsonAlias("file-filters")
        val fileContentFilters: List<ComponentId> = emptyList(),
        @JsonAlias("content-filters")
        val itemContentFilters: List<ComponentId> = emptyList(),
        @JsonDeserialize(`as` = CorePathPattern::class)
        val savePathPattern: PathPattern = CorePathPattern.ORIGIN,
        @JsonDeserialize(`as` = CorePathPattern::class)
        val filenamePattern: PathPattern = CorePathPattern.ORIGIN,
        // 为了兼容
        @JsonAlias("run-after-completion")
        val processListeners: List<ComponentId> = emptyList(),
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        val renameTaskInterval: Duration = Duration.ofMinutes(5),
        val downloadOptions: DownloadOptions = DownloadOptions(),
        val variableConflictStrategy: VariableConflictStrategy = VariableConflictStrategy.SMART,
        val renameTimesThreshold: Int = 3,
        val saveProcessingContent: Boolean = true,
        val itemExpressionExclusions: List<String> = emptyList(),
        val itemExpressionInclusions: List<String> = emptyList(),
        val contentExpressionExclusions: List<String> = emptyList(),
        val contentExpressionInclusions: List<String> = emptyList(),
        val fileExpressionExclusions: List<String> = emptyList(),
        val fileExpressionInclusions: List<String> = emptyList(),
        val variableErrorStrategy: VariableErrorStrategy = VariableErrorStrategy.STAY,
        val touchItemDirectory: Boolean = true,
        val deleteEmptyDirectory: Boolean = true,
        val variableNameReplace: Map<String, String> = emptyMap(),
        val fileTaggers: List<ComponentId> = emptyList(),
        @JsonDeserialize(contentAs = RegexVariableReplacer::class)
        val variableReplacers: List<VariableReplacer> = emptyList(),
        val fileReplacementDecider: ComponentId = ComponentId("never"),
        val fileExistsDetector: ComponentId? = null,
        val fetchLimit: Int = 50,
        /**
         * 从Source获取Items后，更新pointer的模式，true:处理完这一批更新一次，false:处理完一个更新一次
         */
        val pointerBatchMode: Boolean = true,
        val category: String? = null,
        val tags: Set<String> = emptySet(),
        val itemErrorContinue: Boolean = false,
        val fileGrouping: List<FileOptionConfig> = emptyList(),
        val manualSources: List<ComponentId> = emptyList(),
        val channelBufferSize: Int = 20,
        val listenerMode: ListenerMode = ListenerMode.EACH,
        val recordMinimized: Boolean = false
    )

    data class FileOptionConfig(
        val tags: Set<String> = emptySet(),
        val expressionMatching: String? = null,
        val filenamePattern: CorePathPattern? = null,
        val savePathPattern: CorePathPattern? = null,
        val fileContentFilters: List<ComponentId>? = null,
        val fileExpressionExclusions: List<String>? = null,
        val fileExpressionInclusions: List<String>? = null,
        val fileReplacementDecider: ComponentId? = null,
    )
}