package xyz.shoaky.sourcedownloader.sdk.api.qbittorrent

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest

abstract class QbittorrentRequest<T : Any> : BaseRequest<T>() {
    override val mediaType: MediaType = MediaType.APPLICATION_FORM_URLENCODED
    override val httpMethod: HttpMethod = HttpMethod.POST

    @JsonIgnore
    open val authenticationRequired: Boolean = true
}