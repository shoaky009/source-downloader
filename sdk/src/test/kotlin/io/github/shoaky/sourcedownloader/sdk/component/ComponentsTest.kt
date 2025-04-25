package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.*
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ComponentsTest {

    @Test
    fun test() {
        val components = ComponentRootType.fromClass(DownloaderCp::class)
        assertEquals(1, components.size)
        assertEquals(ComponentRootType.DOWNLOADER, components.first())

        val components1 = ComponentRootType.fromClass(MultiCp::class)
        assertEquals(3, components1.size)
        assertContentEquals(
            listOf(ComponentRootType.DOWNLOADER, ComponentRootType.FILE_MOVER, ComponentRootType.TAGGER),
            components1
        )
    }
}

private class DownloaderCp : Downloader {

    override fun submit(task: DownloadTask): Boolean {
        throw NotImplementedError()
    }

    override fun defaultDownloadPath(): Path {
        throw NotImplementedError()
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        throw NotImplementedError()
    }

}

private class MultiCp : TorrentDownloader, FileTagger {

    override fun getTorrentFiles(infoHash: String): List<Path> {
        throw NotImplementedError()
    }

    override fun isFinished(sourceItem: SourceItem): Boolean? {
        throw NotImplementedError()
    }

    override fun submit(task: DownloadTask): Boolean {
        throw NotImplementedError()
    }

    override fun defaultDownloadPath(): Path {
        throw NotImplementedError()
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        throw NotImplementedError()
    }

    override fun batchMove(itemContent: ItemContent): BatchMoveResult {
        throw NotImplementedError()
    }

    override fun move(sourceItem: SourceItem, file: FileContent): Boolean {
        throw NotImplementedError()
    }

    override fun tag(sourceFile: SourceFile): String? {
        throw NotImplementedError()
    }

}