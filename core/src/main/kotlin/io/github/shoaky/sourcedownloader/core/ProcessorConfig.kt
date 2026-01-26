package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.github.shoaky.sourcedownloader.core.component.ComponentId
import io.github.shoaky.sourcedownloader.core.component.ListenerConfig
import io.github.shoaky.sourcedownloader.core.expression.ExpressionType
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import io.github.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import io.github.shoaky.sourcedownloader.core.processor.VariableConflictStrategy
import io.github.shoaky.sourcedownloader.core.processor.VariableProcessOutput
import io.github.shoaky.sourcedownloader.sdk.DownloadOptions
import java.time.Duration

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class ProcessorConfig(
    val name: String,
    val triggers: List<ComponentId> = emptyList(),
    val source: ComponentId,
    val itemFileResolver: ComponentId,
    val downloader: ComponentId,
    val fileMover: ComponentId = ComponentId("mover:general"),
    val savePath: String,
    val options: Options = Options(),
    val enabled: Boolean = true,
    val category: String? = null,
    val tags: Set<String> = emptySet(),
) {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    data class Options(
        val savePathPattern: String = CorePathPattern.origin.pattern,
        val filenamePattern: String = CorePathPattern.origin.pattern,
        val fileTaggers: List<ComponentId> = emptyList(),
        val variableProviders: List<ComponentId> = emptyList(),
        val regexVariableReplacers: List<RegexVariableReplacerConfig> = emptyList(),
        val variableReplacers: List<VariableReplacerConfig> = emptyList(),
        val variableNameReplace: Map<String, String> = emptyMap(),
        val variableErrorStrategy: VariableErrorStrategy = VariableErrorStrategy.STAY,
        val variableConflictStrategy: VariableConflictStrategy = VariableConflictStrategy.SMART,
        val fileExistsDetector: ComponentId? = null,
        val fileReplacementDecider: ComponentId = ComponentId("never"),
        val fileGrouping: List<FileGroupingConfig> = emptyList(),
        val itemGrouping: List<ItemGroupingConfig> = emptyList(),
        val supportWindowsPlatformPath: Boolean = true,
        val variableProcess: List<VariableProcessConfig> = emptyList(),
        val itemFilters: List<ComponentId> = emptyList(),
        val sourceFileFilters: List<ComponentId> = emptyList(),
        val fileContentFilters: List<ComponentId> = emptyList(),
        val itemContentFilters: List<ComponentId> = emptyList(),
        val itemExpressionExclusions: List<String> = emptyList(),
        val itemExpressionInclusions: List<String> = emptyList(),
        val itemContentExpressionExclusions: List<String> = emptyList(),
        val itemContentExpressionInclusions: List<String> = emptyList(),
        val fileContentExpressionExclusions: List<String> = emptyList(),
        val fileContentExpressionInclusions: List<String> = emptyList(),
        val processListeners: List<ListenerConfig> = emptyList(),
        @param:JsonFormat(shape = JsonFormat.Shape.STRING)
        val renameTaskInterval: Duration = Duration.ofMinutes(5),
        val renameTimesThreshold: Int = 3,
        val saveProcessingContent: Boolean = true,
        val preferIdentityFilter: Boolean = false,
        val fetchLimit: Int = 50,
        /**
         * 从Source获取Items后，更新pointer的模式，true:处理完这一批更新一次，false:处理完一个更新一次
         */
        val pointerBatchMode: Boolean = true,
        val itemErrorContinue: Boolean = false,
        val touchItemDirectory: Boolean = true,
        val deleteEmptyDirectory: Boolean = true,
        val parallelism: Int = 1,
        val retryBackoffMills: Long = 5000L,
        val taskGroup: String? = null,
        val channelBufferSize: Int = 20,
        // 下载
        val downloadOptions: DownloadOptions = DownloadOptions(),
        val trimming: List<TrimmingConfig> = emptyList(),
        // 这个限制后面需要根据路径来判断文件系统最大的长度，暂时先手动配置
        val pathNameLengthLimit: Int = 255
    ) {

        // 暂时不提供配置
        @JsonIgnore
        val expression: ExpressionType = ExpressionType.CEL
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class FileGroupingConfig(
        val tags: Set<String> = emptySet(),
        val expressionMatching: String? = null,
        val filenamePattern: String? = null,
        val savePathPattern: String? = null,
        val fileContentFilters: List<ComponentId>? = null,
        val fileContentExpressionExclusions: List<String>? = null,
        val fileContentExpressionInclusions: List<String>? = null,
        val fileReplacementDecider: ComponentId? = null,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ItemGroupingConfig(
        val tags: Set<String> = emptySet(),
        val expressionMatching: String? = null,
        val filenamePattern: String? = null,
        val savePathPattern: String? = null,
        val variableProviders: List<ComponentId>? = null,
        val sourceFilters: List<ComponentId>? = null,
        val itemExpressionExclusions: List<String>? = null,
        val itemExpressionInclusions: List<String>? = null,
    )

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    data class VariableProcessConfig(
        val input: String,
        val chain: List<ComponentId> = emptyList(),
        val output: VariableProcessOutput = VariableProcessOutput(),
        val conditionExpression: String? = null
    )

    data class RegexVariableReplacerConfig(
        val regex: String,
        val replacement: String
    )

    data class TrimmingConfig(
        val variableName: String,
        val trimmers: List<String>
    )
}