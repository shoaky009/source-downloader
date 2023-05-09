package xyz.shoaky.sourcedownloader.external.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference

class TorrentFilePrioRequest(
    val hash: String,
    indexes: List<Int>,
    val priority: Int
) : QbittorrentRequest<String>() {
    val id: String = indexes.joinToString("|")
    override val path: String = "/api/v2/torrents/filePrio"
    override val responseBodyType: TypeReference<String> = stringTypeReference
}