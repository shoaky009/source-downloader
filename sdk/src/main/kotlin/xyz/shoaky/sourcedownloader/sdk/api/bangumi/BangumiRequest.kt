package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest

abstract class BangumiRequest<T : Any> : BaseRequest<T>() {

    override val mediaType: MediaType = MediaType.APPLICATION_JSON

    init {
        httpHeaders.add(HttpHeaders.USER_AGENT, "shoaky009/SourceDownloader/0.1 (Beta) (https://github.com/shoaky009/SourceDownloader)")
        httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
    }
}