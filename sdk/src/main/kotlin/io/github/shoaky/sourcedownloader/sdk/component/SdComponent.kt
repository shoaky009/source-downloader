package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.Predicate
import kotlin.io.path.*
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

/**
 * Trigger the process
 */
interface Trigger : SdComponent, AutoCloseable {

    /**
     * @param task the task to be executed
     */
    fun addTask(task: ProcessorTask)

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
    fun removeTask(task: ProcessorTask): Boolean
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

    /**
     * will be passed to [DownloadOptions.headers]
     * @return the header for the item
     */
    fun headers(sourceItem: SourceItem): Map<String, String> = emptyMap()

    /**
     * 当有限流控制需要分组处理，在被触发时调用会根据该值进行串行化调用
     */
    fun group(): String? {
        return null
    }
}

interface Downloader : SdComponent {

    /**
     * @param task the task to be submitted
     * @return true if the task is submitted successfully
     */
    fun submit(task: DownloadTask): Boolean

    /**
     * @return the default download path
     */
    fun defaultDownloadPath(): Path

    /**
     * Cancel the download task
     */
    fun cancel(sourceItem: SourceItem, files: List<SourceFile>)

}

/**
 *
 */
interface ItemFileResolver : SdComponent {

    /**
     * Resolve files from item, must not duplicate and file name not too long
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
     *
     * @return the variables for the item, if the provider does not support the item, return [PatternVariables.EMPTY]
     */
    fun itemVariables(sourceItem: SourceItem): PatternVariables

    fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>,
    ): List<PatternVariables> {
        return sourceFiles.map { PatternVariables.EMPTY }
    }

    /**
     * @param sourceItem given extra information
     * @param text to extract
     * @return the extracted value
     */
    fun extractFrom(sourceItem: SourceItem, text: String): PatternVariables? {
        return null
    }

    /**
     * Returns the primary variable name, if is null in the variable provider, it will be ignored.
     *
     * @experimental not sure is it good?
     */
    fun primary(): String?
}

/**
 * The bridge between the downloadPath and the savePath
 */
interface FileMover : SdComponent {

    /**
     * @param file the [FileContent] to be moved
     */
    fun move(sourceItem: SourceItem, file: FileContent): Boolean

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
    fun replace(itemContent: ItemContent): Boolean {
        var result = true
        itemContent.fileContents.forEach {
            // TODO existTargetPath标明是否实际存在的还是提前占用的
            val existTargetPath = it.existTargetPath ?: throw IllegalStateException("existTargetPath is null")
            val backupPath = existTargetPath.resolveSibling("${existTargetPath.name}.bak")
            if (existTargetPath.exists()) {
                existTargetPath.moveTo(backupPath)
            }

            try {
                it.fileDownloadPath.moveTo(it.targetPath(), true)
                backupPath.deleteIfExists()
            } catch (e: Throwable) {
                backupPath.moveTo(existTargetPath)
                result = false
            }
        }
        return result
    }

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

    fun pathMetadata(path: Path): SourceFile {
        val fileAttributes = path.readAttributes<BasicFileAttributes>()
        return SourceFile(
            path = path,
            attrs = mapOf(
                "size" to fileAttributes.size(),
                "lastModifiedTime" to fileAttributes.lastModifiedTime().toMillis(),
                "creationTime" to fileAttributes.creationTime().toMillis(),
                "isSymbolicLink" to fileAttributes.isSymbolicLink,
            )
        )
    }
}

interface ProcessListener : SdComponent {

    /**
     * Invoked when the item processing fails.
     * @param sourceItem the item
     * @param throwable the throwable
     */
    fun onItemError(context: ProcessContext, sourceItem: SourceItem, throwable: Throwable) {}

    /**
     * Invoked when the item is processed successfully, which means the item has been downloaded and renamed.
     */
    fun onItemSuccess(context: ProcessContext, itemContent: ItemContent) {}

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
 * @return true if the file should be processed
 */
interface SourceFileFilter : SdComponent, Predicate<SourceFile>

/**
 * @return true if the item should be processed
 */
interface ItemContentFilter : SdComponent, Predicate<ItemContent>

/**
 * @return true if the file should be processed
 */
interface FileContentFilter : SdComponent, Predicate<FileContent>

/**
 * Tag the file
 */
interface FileTagger : SdComponent {

    /**
     * @return the tag of the file, null if no tag
     */
    fun tag(sourceFile: SourceFile): String?
}

/**
 * Decide whether to replace the file
 */
interface FileReplacementDecider : SdComponent {

    /**
     * @param current the current [ItemContent], the [ItemContent.fileContents] always has one [FileContent] element
     * @param before the [ItemContent] before the current one, the [ItemContent.fileContents] also may empty
     * @param existingFile the existing file, [SourceFile.downloadUri] and [SourceFile.data] are always null,
     * [SourceFile.attrs] contains the file metadata 'size' 'lastModifiedTime' 'creationTime'.
     * @return true if the current [ItemContent] should replace
     */
    fun isReplace(current: ItemContent, before: ItemContent?, existingFile: SourceFile): Boolean

}

/**
 * Decide item download or not
 */
interface FileExistsDetector : SdComponent {

    /**
     * @return Key is the path to be detected, Value is the path that is considered to exist
     */
    fun exists(fileMover: FileMover, content: ItemContent): Map<Path, Path?>

}

interface VariableReplacer : SdComponent {

    fun replace(key: String, value: String): String

}

interface Trimmer : SdComponent {

    /**
     * Trim the filename if it exceeds the maximum length
     * @param expectLength The expect length of the value
     * @return The trimmed filename, length can be great than to the expect length
     */
    fun trim(value: String, expectLength: Int): String
}

interface ManualSource : SdComponent {

}