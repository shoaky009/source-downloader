package xyz.shoaky.sourcedownloader.api.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference
import xyz.shoaky.sourcedownloader.sdk.api.HttpMethod

/**
 * https://github.com/qbittorrent/qBittorrent/wiki/WebUI-API-(qBittorrent-4.1)#torrent-management
 */
@Suppress("UNUSED_PARAMETER")
class TorrentInfoRequest(
    filter: String? = null,
    category: String? = null,
    tag: String? = null,
    sort: String? = null,
    reverse: Boolean? = null,
    limit: Int? = null,
    offset: Int? = null,
    hashes: String? = null,
) : QbittorrentRequest<List<TorrentInfo>>() {

    override val path: String = "/api/v2/torrents/info"

    override val responseBodyType: TypeReference<List<TorrentInfo>> = object : TypeReference<List<TorrentInfo>>() {}

    //    override val responseBodyType: TypeReference<String> = stringTypeReference
    override val httpMethod: HttpMethod = HttpMethod.GET

}