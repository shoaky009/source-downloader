package io.github.shoaky.sourcedownloader.external.bbdown

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest

class DeleteTask(
    aid: String,
) : BaseRequest<String>() {

    override val path: String = "/remove-finished/$aid"
    override val responseBodyType: TypeReference<String> = jacksonTypeRef()
    override val httpMethod: String = "GET"
    override val mediaType: MediaType? = null
}