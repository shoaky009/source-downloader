package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Predicate
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

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
 * @param SP the type of the pointer
 */
interface Source<SP : SourcePointer> : SdComponent {

    /**
     * @param limit the max number of items to be fetched, Not necessarily exactly the number of limits returned, but as close as possible.
     */
    fun fetch(pointer: SP, limit: Int = 50): Iterable<PointedItem<ItemPointer>>

    fun defaultPointer(): SP

    @Deprecated("为了能够传递HttpHeader临时瞎定义的后面要改", ReplaceWith("none"))
    fun headers(): Map<String, String> = emptyMap()
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
    fun cancel(sourceItem: SourceItem, files: List<SourceFile>)

}

interface ItemFileResolver : SdComponent {

    /**
     * Resolve files from item, must not duplicate
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
     * @param itemContent the [ItemContent] to be moved
     */
    fun move(itemContent: ItemContent): Boolean

    /**
     * @param paths the paths
     * @return true if the path exists
     */
    fun exists(paths: List<Path>): List<Boolean> {
        return paths.map { it.exists() }
    }

    /**
     * @param path the path to be created
     */
    fun createDirectories(path: Path) {
        path.createDirectories()
    }

    /**
     * @param itemContent the [ItemContent] to be replaced
     */
    fun replace(itemContent: ItemContent): Boolean

    /**
     * @param path the path to be listed
     * @return The absolute paths in the directory
     */
    fun listPath(path: Path): List<Path> {
        if (path.notExists()) {
            return emptyList()
        }
        return Files.list(path).toList()
    }
}

interface ProcessListener : SdComponent {

    /**
     * Invoked when the item processing fails.
     * @param sourceItem the item
     * @param throwable the throwable
     */
    fun onItemError(sourceItem: SourceItem, throwable: Throwable) {}

    /**
     * Invoked when the item is processed successfully, which means the item has been downloaded and renamed.
     */
    fun onItemSuccess(itemContent: ItemContent) {}

    /**
     * Invoked when the process is completed
     * @param processContext the [ProcessContext] of the process
     */
    fun onProcessCompleted(processContext: ProcessContext) {}

}

/**
 * @return true if the item should be processed
 */
interface SourceItemFilter : SdComponent, Predicate<SourceItem>

/**
 * @return true if the item should be processed
 */
interface ItemContentFilter : SdComponent, Predicate<ItemContent>

/**
 * @return true if the file should be processed
 */
interface FileContentFilter : SdComponent, Predicate<FileContent>

interface FileTagger : SdComponent {

    /**
     * @return the tag of the file, null if no tag
     */
    fun tag(fileContent: SourceFile): String?
}

interface FileReplacementDecider : SdComponent {

    /**
     * @param current the current [ItemContent], the [ItemContent.sourceFiles] always has one [FileContent] element
     * @param before the [ItemContent] before the current one, the [ItemContent.sourceFiles] also may empty
     * @return true if the current [ItemContent] should replace
     */
    fun isReplace(current: ItemContent, before: ItemContent?): Boolean

}

/**
 * Decide item download or not
 */
interface FileExistsDetector : SdComponent {

    /**
     * @return true if exists, item will not be downloaded
     */
    fun exists(fileMover: FileMover, content: ItemContent): Map<Path, Boolean>

}

// TODO
interface ManualSource : SdComponent {

}