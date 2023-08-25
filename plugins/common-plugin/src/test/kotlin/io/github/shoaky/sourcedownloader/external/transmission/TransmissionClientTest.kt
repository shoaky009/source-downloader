package io.github.shoaky.sourcedownloader.external.transmission

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.io.path.Path


@Disabled("没有意义")
class TransmissionClientTest {

    private val uri = URI("http://truenas:19091")
    private val client = TransmissionClient(uri, "admin", "admin")

    @Test
    fun session_get() {
        val body = client.execute(SessionGet()).body()
        val arguments = body.arguments
        println(arguments)
    }

    @Test
    fun torrent_add() {
        val body = client.execute(TorrentAdd(
            "https://mikanani.me/Download/20221128/7443ef7a687f1068ee47cf0b9e38a6b2eba6a75b.torrent",
            Path("/downloads"),
        )).body()
        val arguments = body.arguments
        println(arguments)
    }

    @Test
    fun torrent_get() {
        val body = client.execute(TorrentGet(
            listOf("66176f4007e8a3cdd29449c6a0277b10e3eb58f0")
        )).body()
        val arguments = body.arguments
        println(arguments.torrents)
    }

    @Test
    fun torrent_set() {
        val body = client.execute(TorrentSet(
            listOf("7443ef7a687f1068ee47cf0b9e38a6b2eba6a75b"),
            listOf(1)
        )).body()
        val arguments = body.arguments
        println(arguments)
    }

    @Test
    fun location_set() {
        val execute = client.execute(TorrentSetLocation(
            listOf("d1160f9b4c47fe93c6cb84514ef80ebc46667929"),
            Path("/downloads/U149"),
            true
        ))
        println(execute.body().arguments)
    }

    @Test
    fun torrent_rename_path() {
        client.execute(
            TorrentRenamePath(
                listOf("54737f67160c6bb9a7408fd34260593647592265"),
                Path("Hataraku Saibou", "[Nekomoe kissaten&VCB-Studio] Hataraku Saibou [01][BD].mkv"),
                Path("Season 01", "[Nekomoe kissaten&VCB-Studio] Hataraku Saibou [01].mkv")
            )
        )
    }
}