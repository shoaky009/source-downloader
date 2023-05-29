package xyz.shoaky.sourcedownloader.sdk.component

import com.google.common.base.CaseFormat
import xyz.shoaky.sourcedownloader.sdk.*
import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSuperclassOf

sealed interface SdComponent

fun <T : SdComponent> KClass<T>.componentSuperClasses(): List<KClass<out SdComponent>> {
    val result = mutableListOf(this)
    this.allSuperclasses
        .filter { it != SdComponent::class && it != Any::class }
        .filterIsInstanceTo(result)
    return result.toList()
}

enum class Components(
    val klass: KClass<out SdComponent>,
    val names: List<String>
) {

    TRIGGER(Trigger::class, listOf("trigger")),
    SOURCE(Source::class, listOf("source")),
    DOWNLOADER(Downloader::class, listOf("downloader")),
    ITEM_FILE_RESOLVER(ItemFileResolver::class, listOf("item-file-resolver", "file-resolver", "itemFileResolver", "fileResolver")),
    VARIABLE_PROVIDER(VariableProvider::class, listOf("provider", "variable-provider", "variableProvider")),
    FILE_MOVER(FileMover::class, listOf("mover", "file-mover", "fileMover")),
    RUN_AFTER_COMPLETION(RunAfterCompletion::class, listOf("run-after-completion", "run", "runAfterCompletion")),
    SOURCE_ITEM_FILTER(SourceItemFilter::class, listOf("source-item-filter", "item-filter", "sourceItemFilter", "itemFilter")),
    FILE_CONTENT_FILTER(FileContentFilter::class, listOf("file-content-filter", "file-filter", "fileContentFilter", "fileFilter")),
    TAGGER(FileTagger::class, listOf("file-tagger", "tagger")),
    ;

    fun lowerHyphenName(): String {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN,
            this.name
        )
    }

    companion object {

        private val nameMapping: Map<String, Components> = values().flatMap {
            it.names.map { name -> name to it }
        }.toMap()

        fun fromClass(klass: KClass<out SdComponent>): List<Components> {
            if (klass == SdComponent::class) {
                throw ComponentException.other("can not create instance of SdComponent.class")
            }

            return values().filter {
                it.klass.isSuperclassOf(klass)
            }
        }

        fun fromName(name: String): Components? {
            return nameMapping[name]
        }

    }
}

interface Trigger : SdComponent {
    fun addTask(task: Runnable)

    fun start()

    fun stop()

    fun restart() {
        stop()
        start()
    }

    fun removeTask(task: Runnable)
}

interface Source<T : SourceItemPointer> : SdComponent {

    /**
     * @param limit 不一定完全按照limit的数量返回，但尽可能接近
     */
    fun fetch(pointer: T?, limit: Int = 50): Iterable<PointedItem<T>>

}

interface Downloader : SdComponent {

    fun submit(task: DownloadTask)

    fun defaultDownloadPath(): Path

}

interface ItemFileResolver : SdComponent {

    /**
     * Resolve files from item
     * @return Relative paths in the download path
     */
    fun resolveFiles(sourceItem: SourceItem): List<SourceFile>
}

interface VariableProvider : SdComponent {

    /**
     * 变量准确度
     * 0:low
     * 1:med
     * 2:high
     * 3:99.99%准确 一般是通过SourceItem中特定的ID，然后从元数据源中获取的
     */
    val accuracy: Int get() = 1

    fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup
    fun support(item: SourceItem): Boolean

}

interface FileMover : SdComponent {

    fun rename(sourceContent: SourceContent): Boolean

    fun exists(paths: List<Path>): Boolean {
        return paths.all { it.exists() }
    }

    fun createDirectories(path: Path) {
        path.createDirectories()
    }
}

@FunctionalInterface
interface RunAfterCompletion : SdComponent, Consumer<SourceContent>

/**
 * @return true if the item should be processed
 */
interface SourceItemFilter : SdComponent, Predicate<SourceItem>

interface FileContentFilter : SdComponent, Predicate<FileContent>

interface FileTagger : SdComponent {

    /**
     * 只允许标记一个
     */
    fun tag(fileContent: FileContent): String?
}