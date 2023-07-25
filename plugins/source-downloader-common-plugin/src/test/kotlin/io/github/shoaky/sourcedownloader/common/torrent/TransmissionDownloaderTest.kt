package io.github.shoaky.sourcedownloader.common.torrent

import io.github.shoaky.sourcedownloader.external.transmission.TransmissionClient
import io.github.shoaky.sourcedownloader.sdk.DownloadOptions
import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDateTime
import kotlin.io.path.Path

@Disabled("有空再补")
class TransmissionDownloaderTest {

    private val downloader = TransmissionDownloader(
        TransmissionClient(URI("http://truenas:19091"), "admin", "admin")
    )

    @Test
    fun name() {
        val task = DownloadTask(
            SourceItem(
                "[ANi] Dr STONE S3 - Dr. STONE 新石纪 第三季 - 06 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]",
                URI("http://localhost"),
                LocalDateTime.now(),
                "torrent",
                URI("magnet:?xt=urn:btih:54737f67160c6bb9a7408fd34260593647592265")
            ),
            listOf(SourceFile(
                Path("/downloads/[Nekomoe kissaten&VCB-Studio] Hataraku Saibou [Ma10p_1080p]/[Nekomoe kissaten&VCB-Studio] Hataraku Saibou [01][Ma10p_1080p][x265_flac_2ac3].mkv")
            )),
            Path("/downloads"),
            DownloadOptions(tags = listOf("test"))
        )
        downloader.submit(task)
    }
}