package xyz.shoaky.sourcedownloader.external.qbittorrent

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.MediaType
import xyz.shoaky.sourcedownloader.sdk.api.HttpMethod
import java.net.URL

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TorrentsAddRequest(
    urlList: List<URL>,
    @JsonProperty("savepath")
    val savePath: String? = null,
    val category: String? = null,
    val paused: Boolean = false,
    val tags: String? = null,
) : QbittorrentRequest<String>() {

    private val urls: String

    init {
        urls = urlList.joinToString(separator = "\n") { it.toString() }
    }

    override val path: String = "/api/v2/torrents/add"
    override val responseBodyType: TypeReference<String> = stringTypeReference
    override val httpMethod: HttpMethod = HttpMethod.POST
    override val mediaType: MediaType = MediaType.FORM_DATA
}