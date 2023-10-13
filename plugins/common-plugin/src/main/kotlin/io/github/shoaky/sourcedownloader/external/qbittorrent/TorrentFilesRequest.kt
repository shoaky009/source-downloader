package io.github.shoaky.sourcedownloader.external.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.http.BodyWrapper

class TorrentFilesRequest(val hash: String) : QbittorrentRequest<BodyWrapper>() {

    override val path: String = "/api/v2/torrents/files"
    override val responseBodyType: TypeReference<BodyWrapper> = jacksonTypeRef()

}