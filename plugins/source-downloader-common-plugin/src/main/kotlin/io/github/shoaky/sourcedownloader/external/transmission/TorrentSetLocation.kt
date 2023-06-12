package io.github.shoaky.sourcedownloader.external.transmission

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import java.nio.file.Path

class TorrentSetLocation(
    private val ids: List<String> = listOf(),
    private val location: Path,
    private val move: Boolean
) : TransmissionRequest<Any>() {
    override val method: String = "torrent-set-location"
    override val arguments: Map<String, Any?> = buildMap {
        if (ids.isEmpty()) {
            throw IllegalArgumentException("ids must not be empty")
        }
        put("ids", ids)
        put("location", location.toString())
        put("move", move)
    }
    override val responseBodyType: TypeReference<TransmissionResponse<Any>> = jacksonTypeRef()
}