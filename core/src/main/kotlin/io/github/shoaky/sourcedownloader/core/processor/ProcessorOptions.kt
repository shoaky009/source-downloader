package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.component.SimpleFileExistsDetector
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import io.github.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import io.github.shoaky.sourcedownloader.sdk.DownloadOptions
import io.github.shoaky.sourcedownloader.sdk.PathPattern
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.*
import java.time.Duration

data class ProcessorOptions(
    val savePathPattern: PathPattern = CorePathPattern.origin,
    val filenamePattern: PathPattern = CorePathPattern.origin,
    val variableProviders: List<VariableProvider> = emptyList(),
    val processListeners: Map<ListenerMode, List<ProcessListener>> = emptyMap(),
    val sourceItemFilters: List<SourceItemFilter> = emptyList(),
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
    val category: String? = null,
    val tags: Set<String> = emptySet(),
    val itemErrorContinue: Boolean = false,
    val fileExistsDetector: FileExistsDetector = SimpleFileExistsDetector,
    val channelBufferSize: Int = 20,
    val recordMinimized: Boolean = false,
    val parallelism: Int = 1,
    val retryBackoffMills: Long = 5000L,
    val taskGroup: String? = null,
    val variableProcessChain: Map<String, VariableProcessChain> = emptyMap(),
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
    val chain: List<VariableProvider>,
    val output: String
) {

    fun process(value: String): String {
        return chain.fold(value) { acc, provider ->
            provider.extractFrom(acc) ?: acc
        }
    }
}

data class FileOption(
    val savePathPattern: CorePathPattern? = null,
    val filenamePattern: CorePathPattern? = null,
    val fileContentFilters: List<FileContentFilter>? = null,
)

data class ItemOption(
    val savePathPattern: CorePathPattern? = null,
    val filenamePattern: CorePathPattern? = null,
    val sourceItemFilters: List<SourceItemFilter>? = null,
    val variableProviders: List<VariableProvider>? = null,
)