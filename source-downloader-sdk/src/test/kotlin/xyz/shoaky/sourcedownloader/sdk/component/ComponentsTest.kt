package xyz.shoaky.sourcedownloader.sdk.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.FileContent
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import java.nio.file.Path
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ComponentsTest {

    @Test
    fun test() {
        val components = Components.fromClass(DownloaderCp::class)
        assertEquals(1, components.size)
        assertEquals(Components.DOWNLOADER, components.first())

        val components1 = Components.fromClass(MultiCp::class)
        assertEquals(3, components1.size)
        assertContentEquals(listOf(Components.DOWNLOADER, Components.FILE_MOVER, Components.TAGGER), components1)
    }
}

private class DownloaderCp : Downloader {
    override fun submit(task: DownloadTask) {
        TODO("Not yet implemented")
    }

    override fun defaultDownloadPath(): Path {
        TODO("Not yet implemented")
    }

}

private class MultiCp : TorrentDownloader, FileTagger {
    override fun isFinished(task: DownloadTask): Boolean? {
        TODO("Not yet implemented")
    }

    override fun submit(task: DownloadTask) {
        TODO("Not yet implemented")
    }

    override fun defaultDownloadPath(): Path {
        TODO("Not yet implemented")
    }

    override fun rename(sourceContent: SourceContent): Boolean {
        TODO("Not yet implemented")
    }

    override fun tag(fileContent: FileContent): String? {
        TODO("Not yet implemented")
    }

}