package io.github.shoaky.sourcedownloader.external.qbittorrent

import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.http.HookedApiClient
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import io.github.shoaky.sourcedownloader.sdk.util.http.defaultCookieManager
import org.slf4j.LoggerFactory
import java.net.HttpCookie
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class QbittorrentClient(
    private val qbittorrentConfig: QbittorrentConfig
) : HookedApiClient() {

    @Volatile
    private var authenticationLegal: Boolean = false
    private val server = qbittorrentConfig.endpoint

    @Synchronized
    private fun tryLogin() {
        val sidCookie = getSidCookie()
        if (sidCookie != null && authenticationLegal) {
            log.debug("Found sid cookie:{}, skip login", sidCookie.value)
            return
        }

        val loginRequest = LoginRequest(qbittorrentConfig.username, qbittorrentConfig.password)

        execute(server, loginRequest)
        val cookie = getSidCookie()
        if (cookie != null) {
            authenticationLegal = true
            return
        }
        log.warn("未找到qBittorrent sid cookie")
    }

    private fun getSidCookie(): HttpCookie? {
        val cookies = defaultCookieManager.cookieStore[server]
        if (log.isDebugEnabled) {
            log.debug("qBittorrent cookies:${Jackson.toJsonString(cookies)}")
        }
        return cookies.filter { it.hasExpired().not() }.firstOrNull { it.name == SID_COOKIE_NAME }
    }

    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        return super.execute(server, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
        if (request is QbittorrentRequest<*>) {
            if (!request.authenticationRequired) {
                return
            }
            tryLogin()
        }
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
        if (request is QbittorrentRequest<*> && request.authenticationRequired && response.statusCode() == 403) {
            authenticationLegal = false
            log.info("认证非法尝试重新登陆qBittorrent...")
            tryLogin()
        }
        if (response.statusCode() != 200) {
            log.warn(
                """qBittorrent响应非200
                    request:{}
                requestBody:{}
                responseCode:{}
                responseBody:{}
                """, request.path, Jackson.toJsonString(request),
                response.statusCode(), response.body()
            )
        }
    }

    companion object {

        const val SID_COOKIE_NAME = "SID"
        const val SUCCESS_RESPONSE = "Ok."
        private val log = LoggerFactory.getLogger(QbittorrentClient::class.java)
    }

}

data class QbittorrentConfig(
    val endpoint: URI,
    val username: String?,
    val password: String?
)