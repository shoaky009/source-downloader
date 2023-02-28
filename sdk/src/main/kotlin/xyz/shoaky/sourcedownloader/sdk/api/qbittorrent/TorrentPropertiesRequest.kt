package xyz.shoaky.sourcedownloader.sdk.api.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference

class TorrentPropertiesRequest(val hash: String) : QbittorrentRequest<TorrentProperties>() {
    override val path: String = "/api/v2/torrents/properties"
    override val responseBodyType: TypeReference<TorrentProperties> = object : TypeReference<TorrentProperties>() {}
}