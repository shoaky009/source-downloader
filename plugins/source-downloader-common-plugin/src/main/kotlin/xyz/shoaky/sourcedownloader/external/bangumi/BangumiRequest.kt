package xyz.shoaky.sourcedownloader.external.bangumi

import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest

abstract class BangumiRequest<T : Any> : BaseRequest<T>() {

    override val mediaType: MediaType = MediaType.JSON_UTF_8

    init {
        httpHeaders[HttpHeaders.USER_AGENT] = "shoaky009/SourceDownloader/0.1 (Beta) (https://github.com/shoaky009/SourceDownloader)"
        httpHeaders[HttpHeaders.ACCEPT] = MediaType.JSON_UTF_8.toString()
    }
}