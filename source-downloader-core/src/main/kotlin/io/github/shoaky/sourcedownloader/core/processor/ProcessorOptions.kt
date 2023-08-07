package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.component.SimpleItemExistsDetector
import io.github.shoaky.sourcedownloader.core.CorePathPattern
import io.github.shoaky.sourcedownloader.core.VariableReplacer
import io.github.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import io.github.shoaky.sourcedownloader.sdk.DownloadOptions
import io.github.shoaky.sourcedownloader.sdk.PathPattern
import io.github.shoaky.sourcedownloader.sdk.component.*
import java.time.Duration

data class ProcessorOptions(
    val savePathPattern: PathPattern = CorePathPattern.ORIGIN,
    val filenamePattern: PathPattern = CorePathPattern.ORIGIN,
    val variableProviders: List<VariableProvider> = emptyList(),
    val runAfterCompletion: List<RunAfterCompletion> = emptyList(),
    val sourceItemFilters: List<SourceItemFilter> = emptyList(),
    val itemContentFilters: List<ItemContentFilter> = emptyList(),
    val fileContentFilters: List<FileContentFilter> = emptyList(),
    val fileTaggers: List<FileTagger> = emptyList(),
    val variableReplacers: List<VariableReplacer> = emptyList(),
    val fileReplacementDecider: FileReplacementDecider = NeverReplace,
    val taggedFileOptions: Map<String, TaggedFileOptions> = emptyMap(),
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
    val itemErrorContinue: Boolean = true,
    val itemExistsDetector: ItemExistsDetector = SimpleItemExistsDetector
) {

    fun getTaggedOptions(tags: Set<String>): TaggedFileOptions? {
        if (tags.isEmpty()) {
            return null
        }
        tags.forEach { tag ->
            taggedFileOptions[tag]?.let { return it }
        }
        return null
    }
}

data class TaggedFileOptions(
    val savePathPattern: CorePathPattern? = null,
    val filenamePattern: CorePathPattern? = null,
    val fileContentFilters: List<FileContentFilter>? = null,
)