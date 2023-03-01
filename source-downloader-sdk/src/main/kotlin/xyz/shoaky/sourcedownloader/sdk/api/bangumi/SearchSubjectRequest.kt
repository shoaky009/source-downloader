package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import com.fasterxml.jackson.core.type.TypeReference
import xyz.shoaky.sourcedownloader.sdk.api.HttpMethod

/**
 * [document link](https://bangumi.github.io/api/#/%E6%90%9C%E7%B4%A2/searchSubjectByKeywords)
 */
class SearchSubjectRequest(keyword: String) : BangumiRequest<SearchSubjectBody>() {

    private val type = 2
    override val path: String = "/search/subject/${keyword}"
    override val responseBodyType: TypeReference<SearchSubjectBody> = object : TypeReference<SearchSubjectBody>() {}
    override val httpMethod: HttpMethod = HttpMethod.GET

    init {
        addQueryParameter("type", type)
    }
}