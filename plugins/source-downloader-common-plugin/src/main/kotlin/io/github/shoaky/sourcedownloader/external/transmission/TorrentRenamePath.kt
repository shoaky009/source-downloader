package io.github.shoaky.sourcedownloader.external.transmission

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import java.nio.file.Path

class TorrentRenamePath(
    private val ids: List<String> = listOf(),
    private val from: Path,
    private val to: Path,
) : TransmissionRequest<Any>() {
    override val method: String = "torrent-rename-path"
    override val arguments: Map<String, Any?> = buildMap {
        if (ids.isEmpty()) {
            throw IllegalArgumentException("ids must not be empty")
        }
        if (to.isAbsolute || from.isAbsolute) {
            throw IllegalArgumentException("to and from must be relative")
        }
        put("ids", ids)
        put("path", from.toString())
        put("name", to.toString())
    }
    override val responseBodyType: TypeReference<TransmissionResponse<Any>> = jacksonTypeRef()
}