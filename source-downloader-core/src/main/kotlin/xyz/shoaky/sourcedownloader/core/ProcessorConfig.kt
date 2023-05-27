package xyz.shoaky.sourcedownloader.core

// import io.swagger.v3.oas.annotations.media.Schema
// import io.swagger.v3.oas.annotations.media.SchemaProperty
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import xyz.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import xyz.shoaky.sourcedownloader.core.processor.VariableConflictStrategy
import xyz.shoaky.sourcedownloader.sdk.DownloadOptions
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.component.*
import java.nio.file.Path
import java.time.Duration

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class ProcessorConfig(
    val name: String,
    val triggers: List<ComponentId>,
    val source: ComponentId,
    @JsonAlias("variable-providers", "providers")
    val variableProviders: List<ComponentId> = emptyList(),
    @JsonAlias("item-file-resolver", "file-resolver")
    val itemFileResolver: ComponentId,
    val downloader: ComponentId = ComponentId("downloader:url"),
    @JsonAlias("file-mover", "mover")
    val fileMover: ComponentId = ComponentId("mover:general"),
    @JsonAlias("save-path")
    val savePath: Path,
    val options: Options = Options(),
) {

    fun sourceInstanceName(): String {
        return source.getInstanceName(Source::class)
    }

    fun providerInstanceNames(): List<String> {
        return variableProviders.map {
            it.getInstanceName(VariableProvider::class)
        }
    }

    fun fileResolverInstanceName(): String {
        return itemFileResolver.getInstanceName(ItemFileResolver::class)
    }

    fun downloaderInstanceName(): String {
        return downloader.getInstanceName(Downloader::class)
    }

    fun moverInstanceName(): String {
        return fileMover.getInstanceName(FileMover::class)
    }

    fun triggerInstanceNames(): List<String> {
        return triggers.map {
            it.getInstanceName(Trigger::class)
        }
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    data class Options(
        @JsonAlias("source-item-filters", "item-filters")
        val sourceItemFilters: List<ComponentId> = emptyList(),
        @JsonAlias("source-file-filters", "file-filters")
        val sourceFileFilters: List<ComponentId> = emptyList(),
        @JsonAlias("save-path-pattern")
        @JsonDeserialize(`as` = CorePathPattern::class)
        val savePathPattern: PathPattern = CorePathPattern.ORIGIN,
        @JsonAlias("filename-pattern")
        @JsonDeserialize(`as` = CorePathPattern::class)
        val filenamePattern: PathPattern = CorePathPattern.ORIGIN,
        @JsonAlias("run-after-completion")
        val runAfterCompletion: List<ComponentId> = emptyList(),
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonAlias("rename-task-interval")
        val renameTaskInterval: Duration = Duration.ofMinutes(5),
        @JsonAlias("download-options")
        val downloadOptions: DownloadOptions = DownloadOptions(),
        @JsonAlias("variable-conflict-strategy")
        val variableConflictStrategy: VariableConflictStrategy = VariableConflictStrategy.SMART,
        @JsonAlias("touch-item-directory")
        val renameTimesThreshold: Int = 3,
        @JsonAlias("provide-metadata-variables")
        val provideMetadataVariables: Boolean = true,
        @JsonAlias("save-content")
        val saveContent: Boolean = true,
        @JsonAlias("regex-filters")
        val regexFilters: List<String> = emptyList(),
        @JsonAlias("item-expression-exclusions")
        val itemExpressionExclusions: List<String> = emptyList(),
        @JsonAlias("item-expression-inclusions")
        val itemExpressionInclusions: List<String> = emptyList(),
        @JsonAlias("file-expression-exclusions")
        val fileExpressionExclusions: List<String> = emptyList(),
        @JsonAlias("file-expression-inclusions")
        val fileExpressionInclusions: List<String> = emptyList(),
        @JsonAlias("variable-error-strategy")
        val variableErrorStrategy: VariableErrorStrategy = VariableErrorStrategy.STAY,
        @JsonAlias("touch-item-directory")
        val touchItemDirectory: Boolean = true,
        @JsonAlias("delete-empty-directory")
        val deleteEmptyDirectory: Boolean = true,
        @JsonAlias("variable-name-replace")
        val variableNameReplace: Map<String, String> = emptyMap(),
        @JsonAlias("file-taggers")
        val fileTaggers: List<ComponentId> = emptyList(),
        @JsonAlias("tag-filename-pattern")
        @JsonDeserialize(contentAs = CorePathPattern::class)
        val tagFilenamePattern: Map<String, PathPattern> = emptyMap(),
        @JsonAlias("variable-replacers")
        @JsonDeserialize(contentAs = RegexVariableReplacer::class)
        val variableReplacers: List<VariableReplacer> = emptyList()
    )

}


