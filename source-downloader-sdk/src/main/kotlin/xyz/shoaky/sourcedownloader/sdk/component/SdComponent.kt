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

sealed interface SdComponent {

    fun onPropsChange(props: ComponentProps) {}
}

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
    VARIABLE_PROVIDER(VariableProvider::class, listOf("provider", "variable-provider", "variableProvider")),
    FILE_MOVER(FileMover::class, listOf("mover", "file-mover", "fileMover")),
    RUN_AFTER_COMPLETION(RunAfterCompletion::class, listOf("run-after-completion", "run", "runAfterCompletion")),
    SOURCE_ITEM_FILTER(SourceItemFilter::class, listOf("source-item-filter", "item-filter", "sourceItemFilter", "itemFilter")),
    SOURCE_FILE_FILTER(SourceFileFilter::class, listOf("source-file-filter", "file-filter", "sourceFileFilter", "fileFilter"));

    fun lowerHyphenName(): String {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN,
            this.name
        )
    }

    companion object {

        private val nameMapping: Map<String, Components> = values().flatMap {
            it.names.map { name -> name to it }
        }.toMap()

        fun fromClass(klass: KClass<out SdComponent>): Components? {
            return values().firstOrNull { it.klass == klass }
        }

        fun fromName(name: String): Components? {
            return nameMapping[name]
        }
    }
}

interface Trigger : SdComponent {
    fun addTask(runnable: Runnable)

    fun start()

    fun stop()

    fun restart() {
        stop()
        start()
    }
}

interface Source : SdComponent {

    fun fetch(): List<SourceItem>

}

interface Downloader : SdComponent {

    fun submit(task: DownloadTask)

    fun defaultDownloadPath(): Path

    /**
     * Resolve files from item
     * @return Relative paths in the download path
     */
    fun resolveFiles(sourceItem: SourceItem): List<Path>
}

interface VariableProvider : SdComponent {

    /**
     * 变量准确度 当和其他provider变量冲突时会根据该值来决定
     * 0:low
     * 1:med
     * 2:high
     *
     * 考虑过和单个变量绑定，但是感觉没必要
     */
    val accuracy: Int get() = 1

    // 暂时未实现，感觉有点麻烦（异步时需要重跑一次providers）作用也只是省略一点单行数据的占用空间
    val persistentVariable: Boolean get() = true

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

interface CloudFileMover : FileMover

@FunctionalInterface
interface RunAfterCompletion : SdComponent, Consumer<SourceContent>

/**
 * @return true if the item should be processed
 */
interface SourceItemFilter : SdComponent, Predicate<SourceItem>

interface SourceFileFilter : SdComponent, Predicate<Path>

interface FileTagger : SdComponent {

    fun tag(fileContent: FileContent): List<String>
}