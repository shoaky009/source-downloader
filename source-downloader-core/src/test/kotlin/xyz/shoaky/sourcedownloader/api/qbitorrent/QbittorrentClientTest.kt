package xyz.shoaky.sourcedownloader.api.qbitorrent

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.qbittorrent.*
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
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
        val hash = "7443ef7a687f1068ee47cf0b9e38a6b2eba6a75b"
        val old = Path.of("/downloads/[Lilith-Raws] 菜鸟炼金术师开店营业中 / Shinmai Renkinjutsushi no Tenpo Keiei - 09 [Baha][WEB-DL][1080p][AVC AAC][CHT][MP4].mp4")
        val new = Path.of("/downloads/test.mp4")
        val request = TorrentsRenameFileRequest(hash, old.toString(), new.toString())
        val execute = client.execute(request)
        println(execute.body())
    }

    @Test
    fun torrent_info() {
        val execute = client.execute(TorrentInfoRequest(hashes = "8a19b59fc53d15172286ccf6167037538c56b59c"))
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
        val execute = client.execute(request).body()
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