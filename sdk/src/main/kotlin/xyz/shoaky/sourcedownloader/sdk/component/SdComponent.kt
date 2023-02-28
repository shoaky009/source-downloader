package xyz.shoaky.sourcedownloader.sdk.component

import xyz.shoaky.sourcedownloader.sdk.*
import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Predicate

sealed interface SdComponent {

    companion object {

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
}

interface SourceContentCreator : SdComponent {

    fun createSourceGroup(sourceItem: SourceItem): SourceGroup

    fun defaultSavePathPattern(): PathPattern
    fun defaultFilenamePattern(): PathPattern

}

interface FileMover : SdComponent {

    /**
     * 摆个烂附带参数不抽象了
     */
    fun rename(sourceFiles: List<SourceFileContent>, torrentHash: String? = null): Boolean
}

@FunctionalInterface
interface RunAfterCompletion : SdComponent, Consumer<SourceContent>

interface SourceFilter : SdComponent, Predicate<SourceItem>