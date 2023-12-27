package io.github.shoaky.sourcedownloader.external.bangumi

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.http.HttpMethod

class SearchSubjectRequest(
    keyword: String,
    val type: Int = 2,
    val responseGroup: String = "small"
) : BangumiRequest<SearchSubjectBody>() {

    override val path: String = "/search/subject/$keyword"
    override val responseBodyType: TypeReference<SearchSubjectBody> = jacksonTypeRef()
    override val httpMethod: String = HttpMethod.GET.name
}