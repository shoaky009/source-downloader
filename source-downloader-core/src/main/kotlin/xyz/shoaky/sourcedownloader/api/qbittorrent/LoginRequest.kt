package xyz.shoaky.sourcedownloader.api.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.MediaType
import xyz.shoaky.sourcedownloader.sdk.api.HttpMethod

class LoginRequest(val username: String?, val password: String?) : QbittorrentRequest<String>() {
    override val path: String = "/api/v2/auth/login"
    override val responseBodyType: TypeReference<String> = stringTypeReference
    override val httpMethod: HttpMethod = HttpMethod.POST
    override val mediaType: MediaType = MediaType.FORM_DATA
    override val authenticationRequired: Boolean = false

}