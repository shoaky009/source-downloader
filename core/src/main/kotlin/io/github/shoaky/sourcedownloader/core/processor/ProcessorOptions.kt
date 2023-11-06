package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.component.SimpleFileExistsDetector
import io.github.shoaky.sourcedownloader.core.VariableReplacer
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import io.github.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import io.github.shoaky.sourcedownloader.sdk.DownloadOptions
import io.github.shoaky.sourcedownloader.sdk.PathPattern
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.*
import java.time.Duration

data class ProcessorOptions(
    val savePathPattern: PathPattern = CorePathPattern.ORIGIN,
    val filenamePattern: PathPattern = CorePathPattern.ORIGIN,
    val variableProviders: List<VariableProvider> = emptyList(),
    val processListeners: List<ProcessListener> = emptyList(),
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
    val listenerMode: ListenerMode = ListenerMode.EACH,
    val recordMinimized: Boolean = false
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

data class FileOption(
    val savePathPattern: CorePathPattern? = null,
    val filenamePattern: CorePathPattern? = null,
    val fileContentFilters: List<FileContentFilter>? = null,
)

data class ItemOption(
    val sourceItemFilters: List<SourceItemFilter>? = null,
    val variableProviders: List<VariableProvider>? = null
)