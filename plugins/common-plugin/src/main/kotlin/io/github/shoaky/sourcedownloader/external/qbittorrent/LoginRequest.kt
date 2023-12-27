package io.github.shoaky.sourcedownloader.external.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.HttpMethod

class LoginRequest(val username: String?, val password: String?) : QbittorrentRequest<String>() {
    override val path: String = "/api/v2/auth/login"
    override val responseBodyType: TypeReference<String> = stringTypeReference
    override val httpMethod: String = HttpMethod.POST.name
    override val mediaType: MediaType = MediaType.FORM_DATA
    override val authenticationRequired: Boolean = false

}