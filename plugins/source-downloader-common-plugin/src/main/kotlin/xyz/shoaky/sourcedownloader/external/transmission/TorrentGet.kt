package xyz.shoaky.sourcedownloader.external.transmission

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import kotlin.reflect.full.declaredMemberProperties

class TorrentGet(
    private val ids: List<String> = emptyList()
) : TransmissionRequest<TorrentGetResponse>() {

    override val method: String = "torrent-get"
    override val responseBodyType: TypeReference<TransmissionResponse<TorrentGetResponse>> = jacksonTypeRef()
    override val arguments: Map<String, Any?> = buildMap {
        ids.takeIf { it.isNotEmpty() }?.let { put("ids", it) }
        put("fields", fields)
    }

    companion object {
        private val fields = Torrent::class.declaredMemberProperties.map { it.name }
    }
}