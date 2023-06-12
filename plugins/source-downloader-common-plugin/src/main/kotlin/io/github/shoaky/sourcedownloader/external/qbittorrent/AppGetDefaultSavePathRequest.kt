package io.github.shoaky.sourcedownloader.external.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference

class AppGetDefaultSavePathRequest : QbittorrentRequest<String>() {
    override val path: String = "/api/v2/app/defaultSavePath"

    override val responseBodyType: TypeReference<String> = stringTypeReference
}