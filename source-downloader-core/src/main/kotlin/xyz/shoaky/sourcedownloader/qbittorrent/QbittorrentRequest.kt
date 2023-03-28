package xyz.shoaky.sourcedownloader.qbittorrent

import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.net.MediaType
import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest
import xyz.shoaky.sourcedownloader.sdk.api.HttpMethod

abstract class QbittorrentRequest<T : Any> : BaseRequest<T>() {
    override val mediaType: MediaType = MediaType.FORM_DATA
    override val httpMethod: HttpMethod = HttpMethod.POST

    @JsonIgnore
    open val authenticationRequired: Boolean = true
}