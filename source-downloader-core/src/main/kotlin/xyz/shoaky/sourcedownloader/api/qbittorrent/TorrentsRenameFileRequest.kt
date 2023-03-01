package xyz.shoaky.sourcedownloader.api.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference

class TorrentsRenameFileRequest(val hash: String, val oldPath: String, val newPath: String) : QbittorrentRequest<String>() {

    override val path: String = "/api/v2/torrents/renameFile"
    override val responseBodyType: TypeReference<String> = stringTypeReference

}