package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ComponentsTest {

    @Test
    fun test() {
        val components = ComponentTopType.fromClass(DownloaderCp::class)
        assertEquals(1, components.size)
        assertEquals(ComponentTopType.DOWNLOADER, components.first())

        val components1 = ComponentTopType.fromClass(MultiCp::class)
        assertEquals(3, components1.size)
        assertContentEquals(
            listOf(ComponentTopType.DOWNLOADER, ComponentTopType.FILE_MOVER, ComponentTopType.TAGGER),
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

    override fun getPaths(torrentHash: String): List<Path> {
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

    override fun move(itemContent: ItemContent): Boolean {
        throw NotImplementedError()
    }

    override fun tag(fileContent: SourceFile): String? {
        throw NotImplementedError()
    }

}