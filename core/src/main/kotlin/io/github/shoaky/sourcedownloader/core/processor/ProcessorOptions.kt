package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.component.SimpleFileExistsDetector
import io.github.shoaky.sourcedownloader.core.expression.CompiledExpression
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import io.github.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import io.github.shoaky.sourcedownloader.sdk.DownloadOptions
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.*
import java.time.Duration

data class ProcessorOptions(
    val savePathPattern: CorePathPattern = CorePathPattern.origin,
    val filenamePattern: CorePathPattern = CorePathPattern.origin,
    val variableProviders: List<VariableProvider> = emptyList(),
    val processListeners: Map<ListenerMode, List<ProcessListener>> = emptyMap(),
    val itemFilters: List<SourceItemFilter> = emptyList(),
    val sourceFileFilters: List<SourceFileFilter> = emptyList(),
    val itemContentFilters: List<ItemContentFilter> = emptyList(),
    val fileContentFilters: List<FileContentFilter> = emptyList(),
    val fileTaggers: List<FileTagger> = emptyList(),
    val variableReplacers: List<VariableReplacer> = emptyList(),
    val fileReplacementDecider: FileReplacementDecider = NeverReplace,
    val itemGrouping: Map<SourceItemPartition, ItemOption> = emptyMap(),
    val fileGrouping: Map<SourceFilePartition, FileOption> = emptyMap(),
    val saveProcessingContent: Boolean = true,
    val renameTaskInterval: Duration = Duration.ofMinutes(5),
    val downloadOptions: DownloadOptions = DownloadOptions(),
    val variableConflictStrategy: VariableConflictStrategy = VariableConflictStrategy.SMART,
    val renameTimesThreshold: Int = 3,
    val variableErrorStrategy: VariableErrorStrategy = VariableErrorStrategy.STAY,
    val variableNameReplace: Map<String, String> = emptyMap(),
    val fetchLimit: Int = 50,
    val pointerBatchMode: Boolean = true,
    val itemErrorContinue: Boolean = false,
    val fileExistsDetector: FileExistsDetector = SimpleFileExistsDetector,
    val channelBufferSize: Int = 20,
    val parallelism: Int = 1,
    val retryBackoffMills: Long = 5000L,
    val taskGroup: String? = null,
    val variableProcessChain: List<VariableProcessChain> = emptyList(),
) {

    fun matchFileOption(sourceFile: SourceFile): FileOption? {
        return fileGrouping.firstNotNullOfOrNull {
            if (it.key.match(sourceFile)) it else null
        }?.value
    }

    fun matchItemOption(item: SourceItem): ItemOption? {
        return itemGrouping.firstNotNullOfOrNull {
            if (it.key.match(item)) it else null
        }?.value

    }
}

data class VariableProcessChain(
    val input: String,
    val chain: List<VariableProvider>,
    val output: VariableProcessOutput,
    val condition: CompiledExpression<Boolean>? = null
) {

    fun process(sourceItem: SourceItem, value: String, contextVariables: Map<String, Any>): Map<String, String> {
        val tempVars = mutableMapOf<String, String>()
        val processedVar = chain.fold(value) { acc: String?, provider ->
            if (acc == null) return@fold null
            val primary = provider.primary() ?: return@fold null
            val vars = provider.extractFrom(sourceItem, acc)?.variables() ?: return@fold null
            tempVars.putAll(vars)
            return@fold vars[primary] ?: acc
        }

        val result: MutableMap<String, String> = mutableMapOf()
        tempVars.mapNotNull { (key, value) ->
            if (contextVariables.containsKey(key)) return@mapNotNull null
            if (key in output.excludeKeys) return@mapNotNull null
            if (output.includeKeys.isNotEmpty() && key !in output.includeKeys) return@mapNotNull null
            (output.keyMapping[key] ?: key) to value
        }.toMap(result)

        // if (processedVar != null && !contextVariables.containsKey(input)) {
        if (processedVar != null) {
            result[output.keyMapping.getOrDefault(input, input)] = processedVar
        }
        return result
    }
}

data class VariableProcessOutput(
    val keyMapping: Map<String, String> = emptyMap(),
    val excludeKeys: Set<String> = emptySet(),
    val includeKeys: Set<String> = emptySet(),
)

data class FileOption(
    val savePathPattern: CorePathPattern? = null,
    val filenamePattern: CorePathPattern? = null,
    val fileContentFilters: List<FileContentFilter>? = null,
)

data class ItemOption(
    val savePathPattern: CorePathPattern? = null,
    val filenamePattern: CorePathPattern? = null,
    val itemFilters: List<SourceItemFilter>? = null,
    val variableProviders: List<VariableProvider>? = null,
)