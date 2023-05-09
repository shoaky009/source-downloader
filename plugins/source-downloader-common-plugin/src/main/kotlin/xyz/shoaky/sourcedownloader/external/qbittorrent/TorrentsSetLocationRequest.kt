package xyz.shoaky.sourcedownloader.external.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference

class TorrentsSetLocationRequest(hashList: List<String>, val location: String) : QbittorrentRequest<String>() {

    val hashes = hashList.joinToString("|")

    override val path: String = "/api/v2/torrents/setLocation"
    override val responseBodyType: TypeReference<String> = stringTypeReference
}