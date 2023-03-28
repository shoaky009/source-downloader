package xyz.shoaky.sourcedownloader.api.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class TorrentFilesRequest(val hash: String) : QbittorrentRequest<List<TorrentFile>>() {

    override val path: String = "/api/v2/torrents/files"
    override val responseBodyType: TypeReference<List<TorrentFile>> = jacksonTypeRef()

}