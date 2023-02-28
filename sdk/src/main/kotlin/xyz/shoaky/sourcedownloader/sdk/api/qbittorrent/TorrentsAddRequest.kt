package xyz.shoaky.sourcedownloader.sdk.api.qbittorrent

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import java.net.URL

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TorrentsAddRequest(urlList: List<URL>,
                         @JsonProperty("savepath")
                         val savePath: String? = null,
                         val category: String? = null) : QbittorrentRequest<String>() {

    private val urls: String

    init {
        urls = urlList.joinToString(separator = "\n") { it.toString() }
    }

    override val path: String = "/api/v2/torrents/add"
    override val responseBodyType: TypeReference<String> = stringTypeReference
    override val httpMethod: HttpMethod = HttpMethod.POST
    override val mediaType: MediaType = MediaType.APPLICATION_FORM_URLENCODED
}