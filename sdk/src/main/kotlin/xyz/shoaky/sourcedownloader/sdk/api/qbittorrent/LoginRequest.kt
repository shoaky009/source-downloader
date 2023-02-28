package xyz.shoaky.sourcedownloader.sdk.api.qbittorrent

import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

class LoginRequest(val username: String?, val password: String?) : QbittorrentRequest<String>() {
    override val path: String = "/api/v2/auth/login"
    override val responseBodyType: TypeReference<String> = stringTypeReference
    override val httpMethod: HttpMethod = HttpMethod.POST
    override val mediaType: MediaType = MediaType.APPLICATION_FORM_URLENCODED
    override val authenticationRequired: Boolean = false

}