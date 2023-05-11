package xyz.shoaky.sourcedownloader.external.transmission

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

data class TorrentSet(
    private val ids: List<String> = listOf(),
    private val filesUnwanted: List<Int> = listOf(),
) : TransmissionRequest<Any>() {
    override val method: String = "torrent-set"
    override val responseBodyType: TypeReference<TransmissionResponse<Any>> = jacksonTypeRef()
    override val arguments: Map<String, Any> = buildMap {
        if (ids.isEmpty()) {
            throw IllegalArgumentException("ids must not be empty")
        }
        put("ids", ids)
        filesUnwanted.takeIf { it.isNotEmpty() }?.let { put("files-unwanted", it) }
    }
}