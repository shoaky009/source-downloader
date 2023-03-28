package xyz.shoaky.sourcedownloader.api.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class TorrentPropertiesRequest(val hash: String) : QbittorrentRequest<TorrentProperties>() {
    override val path: String = "/api/v2/torrents/properties"
    override val responseBodyType: TypeReference<TorrentProperties> = jacksonTypeRef()
}