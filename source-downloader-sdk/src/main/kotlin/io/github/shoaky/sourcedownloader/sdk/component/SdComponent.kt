package io.github.shoaky.sourcedownloader.sdk.component

import com.fasterxml.jackson.annotation.JsonValue
import com.google.common.base.CaseFormat
import io.github.shoaky.sourcedownloader.sdk.*
import java.net.URI
import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSuperclassOf

/**
 * The base interface of all components
 */
sealed interface SdComponent

fun <T : SdComponent> KClass<T>.componentSuperClasses(): List<KClass<out SdComponent>> {
    val result = mutableListOf(this)
    this.allSuperclasses
        .filter { it != SdComponent::class && it != Any::class }
        .filterIsInstanceTo(result)
    return result.toList()
}

enum class ComponentTopType(
    val klass: KClass<out SdComponent>,
    val names: List<String>
) {

    TRIGGER(Trigger::class, listOf("trigger")),
    SOURCE(Source::class, listOf("source")),
    DOWNLOADER(Downloader::class, listOf("downloader")),
    ITEM_FILE_RESOLVER(
        ItemFileResolver::class,
        listOf("item-file-resolver", "file-resolver", "itemFileResolver", "fileResolver")
    ),
    VARIABLE_PROVIDER(VariableProvider::class, listOf("provider", "variable-provider", "variableProvider")),
    FILE_MOVER(FileMover::class, listOf("mover", "file-mover", "fileMover")),
    RUN_AFTER_COMPLETION(RunAfterCompletion::class, listOf("run-after-completion", "run", "runAfterCompletion")),
    SOURCE_ITEM_FILTER(
        SourceItemFilter::class,
        listOf("source-item-filter", "item-filter", "sourceItemFilter", "itemFilter")
    ),
    SOURCE_CONTENT_FILTER(
        SourceContentFilter::class,
        listOf("source-content-filter", "content-filter", "sourceContentFilter", "contentFilter")
    ),
    FILE_CONTENT_FILTER(
        FileContentFilter::class,
        listOf("file-content-filter", "file-filter", "fileContentFilter", "fileFilter")
    ),
    TAGGER(FileTagger::class, listOf("file-tagger", "tagger")),
    FILE_REPLACEMENT_DECIDER(
        FileReplacementDecider::class,
        listOf("file-replacement-decider", "replacement-decider", "fileReplacementDecider", "replacementDecider")
    ),
    ;

    @JsonValue
    fun lowerHyphenName(): String {
        return CaseFormat.UPPER_UNDERSCORE.to(
            CaseFormat.LOWER_HYPHEN,
            this.name
        )
    }

    companion object {

        private val nameMapping: Map<String, ComponentTopType> = entries.flatMap {
            it.names.map { name -> name to it }
        }.toMap()

        fun fromClass(klass: KClass<out SdComponent>): List<ComponentTopType> {
            if (klass == SdComponent::class) {
                throw ComponentException.other("can not create instance of SdComponent.class")
            }

            return entries.filter {
                it.klass.isSuperclassOf(klass)
            }
        }

        fun fromName(name: String): ComponentTopType? {
            return nameMapping[name]
        }

    }
}

interface Trigger : SdComponent, AutoCloseable {

    /**
     * @param task the task to be executed
     */
    fun addTask(task: Runnable)

    /**
     * Start the trigger
     */
    fun start()

    /**
     * Stop the trigger
     */
    fun stop()

    /**
     * Restart the trigger
     */
    fun restart() {
        stop()
        start()
    }

    /**
     * Close the trigger equivalent to stop
     */
    override fun close() {
        stop()
    }

    /**
     * Remove the task from the trigger
     */
    fun removeTask(task: Runnable)
}

/**
 * @param T the type of the pointer
 */
interface Source<T : SourceItemPointer> : SdComponent {

    /**
     * @param limit the max number of items to be fetched, Not necessarily exactly the number of limits returned, but as close as possible.
     */
    fun fetch(pointer: T?, limit: Int = 50): Iterable<PointedItem<T>>

    // fun defaultPointer(): T
}

interface Downloader : SdComponent {

    /**
     * @param task the task to be submitted
     */
    fun submit(task: DownloadTask)

    /**
     * @return the default download path
     */
    fun defaultDownloadPath(): Path

    /**
     * Cancel the download task
     */
    fun cancel(sourceItem: SourceItem)

}

interface ItemFileResolver : SdComponent {

    /**
     * Resolve files from item
     * @return Relative paths
     */
    fun resolveFiles(sourceItem: SourceItem): List<SourceFile>
}

interface VariableProvider : SdComponent {

    /**
     * 0:low
     * 1:med
     * 2:high
     * 3:99.99% Completely accurate, implemented by getting ID from [SourceItem]
     *
     * @return the accuracy of the variable provider
     */
    val accuracy: Int get() = 1

    /**
     * @return the variables for the item
     */
    fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup

    /**
     * @return true if the provider can provide variables for the item
     */
    fun support(item: SourceItem): Boolean

}

interface FileMover : SdComponent {

    /**
     * @param sourceContent the [SourceContent] to be moved
     */
    fun move(sourceContent: SourceContent): Boolean

    /**
     * @param paths the target paths
     */
    fun exists(paths: List<Path>): Boolean {
        return paths.all { it.exists() }
    }

    /**
     * @param path the path to be created
     */
    fun createDirectories(path: Path) {
        path.createDirectories()
    }

    /**
     * @param sourceContent the [SourceContent] to be replaced
     */
    fun replace(sourceContent: SourceContent): Boolean
}

interface RunAfterCompletion : SdComponent, Consumer<SourceContent>

/**
 * @return true if the item should be processed
 */
interface SourceItemFilter : SdComponent, Predicate<SourceItem>

/**
 * @return true if the item should be processed
 */
interface SourceContentFilter : SdComponent, Predicate<SourceContent>

/**
 * @return true if the file should be processed
 */
interface FileContentFilter : SdComponent, Predicate<FileContent>

interface FileTagger : SdComponent {

    /**
     * @return the tag of the file, null if no tag
     */
    fun tag(fileContent: FileContent): String?
}

interface FileReplacementDecider : SdComponent {

    /**
     * @param current the current [SourceContent], the [SourceContent.sourceFiles] always has one [FileContent] element
     * @param before the [SourceContent] before the current one, the [SourceContent.sourceFiles] also may empty
     * @return true if the current [SourceContent] should replace
     */
    fun isReplace(current: SourceContent, before: SourceContent?): Boolean

}

// TODO
interface ManualSource : SdComponent {

    /**
     * @return Provide matched [uri] JavaScript code to convert page to [SourceItem],
     * basically just some selector values mapped to the [SourceItem] structure. null if not matched
     */
    fun getScript(uri: URI): SourceItemConvertScript?

}