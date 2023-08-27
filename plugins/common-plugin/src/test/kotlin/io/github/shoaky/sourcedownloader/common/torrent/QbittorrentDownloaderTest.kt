package io.github.shoaky.sourcedownloader.common.torrent

import io.github.shoaky.sourcedownloader.external.qbittorrent.QbittorrentClient
import io.github.shoaky.sourcedownloader.external.qbittorrent.TorrentInfo
import io.github.shoaky.sourcedownloader.external.qbittorrent.TorrentInfoRequest
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import org.apache.commons.lang3.NotImplementedException
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.util.*
import javax.net.ssl.SSLSession

class QbittorrentDownloaderTest {

    private val mockClient = Mockito.mock(QbittorrentClient::class.java)
    private val downloader = QbittorrentDownloader(mockClient)

    @Test
    fun should_finished() {
        val torrentHash = "f3bcd4831746caac97212896caa41f4e24d0ae88"
        Mockito.`when`(mockClient.execute(
            Mockito.argThat<TorrentInfoRequest> {
                it.hashes == torrentHash
            }
        )).thenReturn(
            R(listOf(TorrentInfo(0, torrentHash, 305807162L, 1.0f)))
        )

        val sourceItem = SourceItem(
            "",
            URI("https://mikanani.me/Download/20230308/f3bcd4831746caac97212896caa41f4e24d0ae88"),
            LocalDateTime.now(), "torrent",
            URI("https://mikanani.me/Download/20230308/f3bcd4831746caac97212896caa41f4e24d0ae88.torrent")
        )
        val finished = downloader.isFinished(sourceItem) ?: false
        assert(finished)
    }

    @Test
    fun should_not_finished() {
        val torrentHash = "f3bcd4831746caac97212896caa41f4e24d0ae88"
        Mockito.`when`(mockClient.execute(
            Mockito.argThat<TorrentInfoRequest> {
                it.hashes == torrentHash
            }
        )).thenReturn(
            R(listOf(TorrentInfo(0, torrentHash, 305807162L, 0.95f)))
        )

        val sourceItem = SourceItem(
            "",
            URI("https://mikanani.me/Download/20230308/f3bcd4831746caac97212896caa41f4e24d0ae88"),
            LocalDateTime.now(), "torrent",
            URI("https://mikanani.me/Download/20230308/f3bcd4831746caac97212896caa41f4e24d0ae88.torrent")
        )
        val finished = downloader.isFinished(sourceItem) ?: false
        assert(!finished)
    }

}

class R<T>(private val body: T) : HttpResponse<T> {
    override fun statusCode(): Int {
        return 200
    }

    override fun request(): HttpRequest {
        throw NotImplementedException()
    }

    override fun previousResponse(): Optional<HttpResponse<T>> {
        throw NotImplementedException()
    }

    override fun headers(): HttpHeaders {
        throw NotImplementedException()
    }

    override fun body(): T {
        return body
    }

    override fun sslSession(): Optional<SSLSession> {
        throw NotImplementedException()
    }

    override fun uri(): URI {
        throw NotImplementedException()
    }

    override fun version(): HttpClient.Version {
        throw NotImplementedException()
    }

}