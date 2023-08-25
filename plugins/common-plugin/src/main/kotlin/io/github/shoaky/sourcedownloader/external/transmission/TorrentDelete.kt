package io.github.shoaky.sourcedownloader.external.transmission

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class TorrentDelete(
    private val ids: List<String>,
    private val deleteLocalData: Boolean = false
) : TransmissionRequest<TorrentGetResponse>() {

    override val method: String = "torrent-remove"
    override val responseBodyType: TypeReference<TransmissionResponse<TorrentGetResponse>> = jacksonTypeRef()
    override val arguments: Map<String, Any?> = buildMap {
        ids.takeIf { it.isNotEmpty() }?.let { put("ids", it) }
        put("delete-local-data", deleteLocalData)
    }

}
