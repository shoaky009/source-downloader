package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import xyz.shoaky.sourcedownloader.sdk.api.HttpMethod

class SearchSubjectRequest(
    keyword: String,
    val type: Int = 2,
    val responseGroup: String = "small"
) : BangumiRequest<SearchSubjectBody>() {

    override val path: String = "/search/subject/$keyword"
    override val responseBodyType: TypeReference<SearchSubjectBody> = jacksonTypeRef()
    override val httpMethod: HttpMethod = HttpMethod.GET
}