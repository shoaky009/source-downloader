package xyz.shoaky.sourcedownloader.api.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference

class TorrentFilesRequest(val hash: String) : QbittorrentRequest<List<TorrentFile>>() {

    override val path: String = "/api/v2/torrents/files"
    override val responseBodyType: TypeReference<List<TorrentFile>> = object : TypeReference<List<TorrentFile>>() {}

}