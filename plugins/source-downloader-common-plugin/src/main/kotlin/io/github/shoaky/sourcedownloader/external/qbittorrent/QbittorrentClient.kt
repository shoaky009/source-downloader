package io.github.shoaky.sourcedownloader.external.qbittorrent

import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.api.HookedApiClient
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import io.github.shoaky.sourcedownloader.sdk.util.http.defaultCookieManager
import org.slf4j.LoggerFactory
import java.net.HttpCookie
import java.net.URL
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class QbittorrentClient(private val qbittorrentConfig: QbittorrentConfig) : HookedApiClient() {

    @Volatile
    private var authenticationLegal: Boolean = false
    private val endpoint = qbittorrentConfig.url.toURI()

    @Synchronized
    private fun tryLogin() {
        val sidCookie = getSidCookie()
        if (sidCookie != null && authenticationLegal) {
            log.debug("sidCookie:${sidCookie.value}")
            return
        }

        val loginRequest = LoginRequest(qbittorrentConfig.username, qbittorrentConfig.password)

        execute(endpoint, loginRequest)
        val cookie = getSidCookie()
        if (cookie != null) {
            authenticationLegal = true
            return
        }
        log.warn("未找到qBittorrent sid cookie")
    }

    fun <R : BaseRequest<T>, T : Any> execute(request: R): HttpResponse<T> {
        return super.execute(endpoint, request)
    }

    private fun getSidCookie(): HttpCookie? {
        val cookies = defaultCookieManager.cookieStore.get(endpoint)
        if (log.isDebugEnabled) {
            log.debug("qBittorrent cookies:${Jackson.toJsonString(cookies)}")
        }
        return cookies.filter { it.hasExpired().not() }.firstOrNull { it.name == sidCookieName }
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
        if (request is QbittorrentRequest<*>) {
            if (request.authenticationRequired && response.statusCode() == 403) {
                authenticationLegal = false
                log.info("认证非法尝试重新登陆qBittorrent...")
                tryLogin()
            }
        }
        if (response.statusCode() != 200) {
            log.info(
                """qBittorrent响应非200
                    request:{}
                requestBody:{}
                responseCode:{}
                responseBody:{}
                """, request.path, Jackson.toJsonString(request),
                response.statusCode(), response.body())
        }
    }

    companion object {

        const val sidCookieName = "SID"
        const val successResponse = "Ok."
        private val log = LoggerFactory.getLogger(QbittorrentClient::class.java)
    }

}

data class QbittorrentConfig(
    val url: URL,
    val username: String?,
    val password: String?
)