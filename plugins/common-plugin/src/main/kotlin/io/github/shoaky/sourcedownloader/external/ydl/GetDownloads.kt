package io.github.shoaky.sourcedownloader.external.ydl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest

class GetDownloads : BaseRequest<DownloadsResponse>() {

    override val path: String = "/api/downloads"
    override val responseBodyType: TypeReference<DownloadsResponse> = jacksonTypeRef()
    override val httpMethod: String = "POST"
    override val mediaType: MediaType = MediaType.JSON_UTF_8

}