package xyz.shoaky.sourcedownloader.sdk.component

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceFileContent
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.TorrentDownloader
import java.nio.file.Path

class RuleTest {

    @Test
    fun given_same_type_should_expected() {
        val componentRule = ComponentRule.allowDownloader(TorrentDownloader::class)
        assert(componentRule.isSameType(TestDownloader))
    }

    @Test
    fun given_diff_type_should_expected() {
        val componentRule = ComponentRule.allowDownloader(TorrentDownloader::class)
        assert(!componentRule.isSameType(Mover))
    }

    @Test
    fun given_diff_type_should_throws() {
        val componentRule = ComponentRule.allowDownloader(TorrentDownloader::class)
        assertThrows<ComponentException> {
            componentRule.verify(TestDownloader)
        }
    }

    @Test
    fun given_same_type_should_not_throws() {
        val componentRule = ComponentRule.allowDownloader(TorrentDownloader::class)
        assertDoesNotThrow {
            componentRule.verify(TestTorrentDownloader)
        }
    }

}

object Mover : FileMover {
    override fun rename(sourceFiles: List<SourceFileContent>, torrentHash: String?): Boolean {
        TODO("Not yet implemented")
    }
}

object TestDownloader : Downloader {
    override fun submit(task: DownloadTask) {
        TODO("Not yet implemented")
    }

    override fun defaultDownloadPath(): Path {
        TODO("Not yet implemented")
    }

    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        TODO("Not yet implemented")
    }

}

object TestTorrentDownloader : TorrentDownloader {
    override fun isFinished(task: DownloadTask): Boolean {
        TODO("Not yet implemented")
    }

    override fun submit(task: DownloadTask) {
        TODO("Not yet implemented")
    }

    override fun defaultDownloadPath(): Path {
        TODO("Not yet implemented")
    }

    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        TODO("Not yet implemented")
    }

    override fun rename(sourceFiles: List<SourceFileContent>, torrentHash: String?): Boolean {
        TODO("Not yet implemented")
    }

}