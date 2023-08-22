package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.component.SimpleItemExistsDetector
import io.github.shoaky.sourcedownloader.core.CorePathPattern
import io.github.shoaky.sourcedownloader.core.VariableReplacer
import io.github.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import io.github.shoaky.sourcedownloader.sdk.DownloadOptions
import io.github.shoaky.sourcedownloader.sdk.PathPattern
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.*
import io.github.shoaky.sourcedownloader.util.scriptHost
import org.projectnessie.cel.checker.Decls
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
    // val taggedFileOptions: Map<String, FileOptions> = emptyMap(),
    val fileGrouping: Map<SourceFileMatcher, FileOption> = emptyMap(),
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
    val itemExistsDetector: ItemExistsDetector = SimpleItemExistsDetector,
) {

    fun matchFileOption(sourceFile: SourceFile): FileOption? {
        return fileGrouping.firstNotNullOfOrNull {
            if (it.key.match(sourceFile)) it else null
        }?.value
    }
}

interface SourceFileMatcher {

    fun match(sourceFile: SourceFile): Boolean

}

class ExpressionSourceFileMatcher(
    expression: String
) : SourceFileMatcher {

    private val script = scriptHost.buildScript(expression).withDeclarations(
        Decls.newVar("tags", Decls.newListType(Decls.String)),
        Decls.newVar("attrs", Decls.newMapType(Decls.String, Decls.Dyn))
    ).build()

    override fun match(sourceFile: SourceFile): Boolean {
        return script.execute(Boolean::class.java, mapOf(
            "tags" to sourceFile.tags,
            "attrs" to sourceFile.attrs)
        )
    }
}

class TagSourceFileMatcher(
    private val tags: Set<String>
) : SourceFileMatcher {

    override fun match(sourceFile: SourceFile): Boolean {
        return sourceFile.tags.containsAll(tags)
    }
}

data class FileOption(
    val savePathPattern: CorePathPattern? = null,
    val filenamePattern: CorePathPattern? = null,
    val fileContentFilters: List<FileContentFilter>? = null,
)