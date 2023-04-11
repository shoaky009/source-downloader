package xyz.shoaky.sourcedownloader.sdk.component

import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.SourceItemGroup
import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

sealed interface SdComponent

fun <T : SdComponent> KClass<T>.componentSuperClasses(): List<KClass<out SdComponent>> {
    val result = mutableListOf(this)
    this.allSuperclasses
        .filter { it != SdComponent::class && it != Any::class }
        .filterIsInstanceTo(result)
    return result.toList()
}

enum class Components(val klass: KClass<out SdComponent>) {
    TRIGGER(Trigger::class),
    SOURCE(Source::class),
    DOWNLOADER(Downloader::class),
    VARIABLE_PROVIDER(VariableProvider::class),
    FILE_MOVER(FileMover::class),
    RUN_AFTER_COMPLETION(RunAfterCompletion::class),
    SOURCE_ITEM_FILTER(SourceItemFilter::class),
    SOURCE_FILE_FILTER(SourceFileFilter::class)
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
}

@FunctionalInterface
interface RunAfterCompletion : SdComponent, Consumer<SourceContent>

/**
 * @return true if the item should be processed
 */
interface SourceItemFilter : SdComponent, Predicate<SourceItem>

interface SourceFileFilter : SdComponent, Predicate<Path>