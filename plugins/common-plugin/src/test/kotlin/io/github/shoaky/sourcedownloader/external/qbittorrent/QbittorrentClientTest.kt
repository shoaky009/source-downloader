package io.github.shoaky.sourcedownloader.external.qbittorrent

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URL
import java.nio.file.Path

@Disabled("依赖于qBittorrent应用测试才有意义")
class QbittorrentClientTest {

    private val config = QbittorrentConfig(URL("http://truenas:10095"), "admin", "adminadmin")
    private val client = QbittorrentClient(config)

    @Test
    fun torrent_add() {
        val url = URL("https://mikanani.me/Download/20221128/7443ef7a687f1068ee47cf0b9e38a6b2eba6a75b.torrent")
        val execute = client.execute(TorrentsAddRequest(listOf(url)))
        println(execute.body())
    }

    @Test
    fun get_default_save_path() {
        val execute = client.execute(AppGetDefaultSavePathRequest())
        println(execute.body())
    }

    @Test
    fun rename_file() {
        val hash = "026f8170d2251e8f9b6cde7cf782760aa5c6b70f"
        val old = Path.of("[DMG][REVENGER][01-12 END][1080P][GB][MP4]/[DMG][REVENGER][01][1080P][GB].mp4")
        val new = Path.of("Test/[DMG][REVENGER][01][1080P][GB].mp4")
        val request = TorrentsRenameFileRequest(hash, old.toString(), new.toString())
        val execute = client.execute(request)
        println(execute.body())
    }

    @Test
    fun torrent_info() {
        val execute = client.execute(TorrentInfoRequest(hashes = "a8eeaa9978a0af5b20a0f94c5b025d1fe898cc29"))
        println(Jackson.toJsonString(execute.body()))
    }

    @Test
    fun torrent_set_location() {
        val request = TorrentsSetLocationRequest(listOf("ef1ca7fac7ccfa7b7f47780da056d5e5e74c8fb3"),
            "/mnt/test/向山进发 Next Summit/Season 1")
        val execute = client.execute(request)
        println(Jackson.toJsonString(execute))
    }

    @Test
    fun torrent_get_files() {
        val request = TorrentFilesRequest("2ae25932f3b02800f191a08f395cc7ee920ce117")
        val execute = client.execute(request).body().parseJson(jacksonTypeRef<String>())
        println(Jackson.toJsonString(execute))
    }

    @Test
    fun torrent_set_files_prio() {
        val request = TorrentFilePrioRequest("2ae25932f3b02800f191a08f395cc7ee920ce117",
            listOf(0),
            0
        )
        val execute = client.execute(request).body()
        println(Jackson.toJsonString(execute))
    }
}