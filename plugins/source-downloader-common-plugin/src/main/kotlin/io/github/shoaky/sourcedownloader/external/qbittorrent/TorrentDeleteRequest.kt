package io.github.shoaky.sourcedownloader.external.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference

class TorrentDeleteRequest(
    hashes: List<String>,
    deleteFiles: Boolean = false
) : QbittorrentRequest<String>() {

    override val path: String = "/api/v2/torrents/delete?hashes=${hashes.joinToString("|")}&deleteFiles=$deleteFiles"
    override val responseBodyType: TypeReference<String> = stringTypeReference
}