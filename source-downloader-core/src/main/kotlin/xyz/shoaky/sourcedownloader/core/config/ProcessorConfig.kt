package xyz.shoaky.sourcedownloader.core.config

// import io.swagger.v3.oas.annotations.media.Schema
// import io.swagger.v3.oas.annotations.media.SchemaProperty
import com.fasterxml.jackson.annotation.*
import xyz.shoaky.sourcedownloader.core.file.ParsingFailedStrategy
import xyz.shoaky.sourcedownloader.core.file.RenameMode
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.component.*
import java.nio.file.Path
import java.time.Duration
import kotlin.reflect.KClass

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class ProcessorConfig(
    val name: String,
    val triggers: List<ComponentId>,
    val source: ComponentId,
    val providers: List<ComponentId> = emptyList(),
    val downloader: ComponentId = ComponentId("downloader:url"),
    val mover: ComponentId = ComponentId("mover:general"),
    @JsonAlias("savePath", "save-path")
    val savePath: Path,
    @JsonAlias("sourceItemFilters", "source-item-filters", "item-filters")
    val sourceItemFilters: List<ComponentId> = emptyList(),
    @JsonAlias("sourceFileFilters", "source-file-filters", "file-filters")
    val sourceFileFilters: List<ComponentId> = emptyList(),
    val options: Options = Options(),
) {

    fun sourceInstanceName(): String {
        return source.getInstanceName(Source::class)
    }

    fun providerInstanceNames(): List<String> {
        return providers.map {
            it.getInstanceName(VariableProvider::class)
        }
    }

    fun downloaderInstanceName(): String {
        return downloader.getInstanceName(Downloader::class)
    }

    fun moverInstanceName(): String {
        return mover.getInstanceName(FileMover::class)
    }

    fun triggerInstanceNames(): List<String> {
        return triggers.map {
            it.getInstanceName(Trigger::class)
        }
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    data class Options(
        val fileSavePathPattern: PathPattern = PathPattern.ORIGIN,
        val filenamePattern: PathPattern = PathPattern.ORIGIN,
        val runAfterCompletion: List<ComponentId> = emptyList(),
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        val renameTaskInterval: Duration = Duration.ofMinutes(5),
        val downloadCategory: String? = null,
        // NOT IMPLEMENTED
        val variableConflictDecision: VariableConflictDecision = VariableConflictDecision.SMART,
        // 修改文件夹创建时间
        val renameTimesThreshold: Int = 3,
        val provideMetadataVariables: Boolean = true,
        val saveContent: Boolean = true,
        val renameMode: RenameMode = RenameMode.MOVE,
        val regexFilters: List<String> = emptyList(),
        val itemExpressionExclusions: List<String> = emptyList(),
        val itemExpressionInclusions: List<String> = emptyList(),
        val fileExpressionExclusions: List<String> = emptyList(),
        val fileExpressionInclusions: List<String> = emptyList(),
        val parsingFailedStrategy: ParsingFailedStrategy = ParsingFailedStrategy.USE_ORIGINAL_FILENAME,
        val touchItemDirectory: Boolean = true,
        val cleanEmptyDirectory: Boolean = true,
        val variablesNameMapping: Map<String, String> = emptyMap(),
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
        @get:JsonValue
        val id: String,
    ) {
        fun <T : SdComponent> getInstanceName(klass: KClass<T>): String {
            return getComponentType(klass).instanceName(name())
        }

        fun <T : SdComponent> getComponentType(klass: KClass<T>): ComponentType {
            return ComponentType(typeName(), klass)
        }

        fun name(): String {
            return id.split(":").last()
        }

        fun typeName(): String {
            val split = id.split(":")
            if (split.isEmpty()) {
                throw RuntimeException("组件ID配置错误:${id}")
            }
            return split.first()
        }
    }
}


