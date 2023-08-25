package io.github.shoaky.sourcedownloader.external.transmission

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import java.nio.file.Path

class TorrentAdd(
    private val filename: String,
    private val downloadPath: Path?,
    private val labels: List<String> = emptyList(),
    private val paused: Boolean = false,
    private val filesUnwanted: List<Int> = emptyList(),
    private val filesWanted: List<Int> = emptyList(),
) : TransmissionRequest<TorrentAddResponse>() {
    override val method: String = "torrent-add"
    override val arguments: Map<String, Any> = buildMap {
        put("filename", filename)
        downloadPath?.let { put("download-dir", it.toString()) }
        labels.takeIf { it.isNotEmpty() }?.let { put("labels", it) }
        put("paused", paused)
        filesUnwanted.takeIf { it.isNotEmpty() }?.let { put("files-unwanted", it.map { p -> p.toString() }) }
        filesWanted.takeIf { it.isNotEmpty() }?.let { put("files-wanted", it.map { p -> p.toString() }) }
    }
    override val responseBodyType: TypeReference<TransmissionResponse<TorrentAddResponse>> = jacksonTypeRef()
}

data class TorrentAddResponse(
    @JsonProperty("torrent-added")
    val torrentAdded: TorrentIdInfo? = null,
    @JsonProperty("torrent-duplicate")
    val torrentDuplicate: TorrentIdInfo? = null,
) {
    fun getHash(): String {
        return torrentAdded?.hashString
            ?: torrentDuplicate?.hashString
            ?: throw IllegalStateException("hash not found")
    }
}

data class TorrentIdInfo(
    val id: Long,
    val name: String,
    val hashString: String,
)