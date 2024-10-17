package io.github.shoaky.sourcedownloader.external.webdav

import com.google.common.net.HttpHeaders
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.http.HookedApiClient
import io.github.shoaky.sourcedownloader.sdk.util.encodeBase64
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class WebdavClient(
    private val server: URI,
    private val username: String? = null,
    private val password: String? = null,
) : HookedApiClient() {

    private val authorization = "$username:$password".encodeBase64()
    val webdavPath: String = server.path.removeSuffix("/")

    fun <R : WebdavReq<T>, T : Any> execute(request: R): HttpResponse<T> {
        return execute(server, request)
    }

    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
        // 以后支持更多的方式
        username?.apply {
            requestBuilder.header(HttpHeaders.AUTHORIZATION, "Basic $authorization")
        }
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {

    }

}
