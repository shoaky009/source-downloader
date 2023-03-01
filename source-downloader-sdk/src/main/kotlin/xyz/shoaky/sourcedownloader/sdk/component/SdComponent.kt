package xyz.shoaky.sourcedownloader.sdk.component

import xyz.shoaky.sourcedownloader.sdk.*
import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

sealed interface SdComponent

fun <T : SdComponent> KClass<T>.componentClasses(): List<KClass<out SdComponent>> {
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
    SOURCE_CONTENT_CREATOR(SourceContentCreator::class),
    FILE_MOVER(FileMover::class),
    RUN_AFTER_COMPLETION(RunAfterCompletion::class),
    SOURCE_FILTER(SourceFilter::class)
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

interface SourceContentCreator : SdComponent {

    fun createSourceGroup(sourceItem: SourceItem): SourceGroup

    fun defaultSavePathPattern(): PathPattern
    fun defaultFilenamePattern(): PathPattern

}

interface FileMover : SdComponent {

    fun rename(sourceFiles: List<SourceFileContent>, torrentHash: String? = null): Boolean
}

@FunctionalInterface
interface RunAfterCompletion : SdComponent, Consumer<SourceContent>

interface SourceFilter : SdComponent, Predicate<SourceItem>