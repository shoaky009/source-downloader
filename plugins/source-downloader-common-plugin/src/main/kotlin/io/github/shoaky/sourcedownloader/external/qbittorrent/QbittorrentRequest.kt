package io.github.shoaky.sourcedownloader.external.qbittorrent

import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.api.HttpMethod

abstract class QbittorrentRequest<T : Any> : BaseRequest<T>() {
    override val mediaType: MediaType = MediaType.FORM_DATA
    override val httpMethod: HttpMethod = HttpMethod.POST

    @JsonIgnore
    open val authenticationRequired: Boolean = true
}