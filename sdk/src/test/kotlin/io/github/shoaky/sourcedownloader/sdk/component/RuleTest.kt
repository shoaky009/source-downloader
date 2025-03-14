package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
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

    override fun move(sourceItem: SourceItem, file: FileContent): Boolean {
        return true
    }

    override fun replace(itemContent: ItemContent): Boolean {
        return true
    }
}

object TestDownloader : Downloader {

    override fun submit(task: DownloadTask): Boolean {
        return true
    }

    override fun defaultDownloadPath(): Path {
        return Path.of("test")
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        NotImplementedError()
    }

}

object TestTorrentDownloader : TorrentDownloader {

    override fun getTorrentFiles(infoHash: String): List<Path> {
        return emptyList()
    }

    override fun isFinished(sourceItem: SourceItem): Boolean {
        return true
    }

    override fun submit(task: DownloadTask): Boolean {
        return true
    }

    override fun defaultDownloadPath(): Path {
        return Path.of("test")
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        NotImplementedError()
    }

    override fun batchMove(itemContent: ItemContent): BatchMoveResult {
        return BatchMoveResult(true, emptyList())
    }

    override fun move(sourceItem: SourceItem, file: FileContent): Boolean {
        return true
    }
}