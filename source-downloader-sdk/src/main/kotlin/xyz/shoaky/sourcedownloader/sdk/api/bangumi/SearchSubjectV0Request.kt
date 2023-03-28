package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import xyz.shoaky.sourcedownloader.sdk.api.HttpMethod

/**
 * [document link](https://bangumi.github.io/api/#/%E6%90%9C%E7%B4%A2/searchSubjectByKeywords)
 */
class SearchSubjectV0Request(
    val keyword: String,
    type: Int = 2,
    nsfw: Boolean = false,
) : BangumiRequest<SearchSubjectV0Body>() {

    val filter = mapOf(
        "type" to listOf(type),
        "nsfw" to nsfw
    )

    override val path: String = "/v0/search/subjects"
    override val responseBodyType: TypeReference<SearchSubjectV0Body> = jacksonTypeRef()
    override val httpMethod: HttpMethod = HttpMethod.POST

}