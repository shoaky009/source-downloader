package io.github.shoaky.sourcedownloader.external.bangumi

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.http.HttpMethod
import io.github.shoaky.sourcedownloader.sdk.util.http.CommonBodyHandler
import java.net.URLEncoder

class SearchSubjectRequest(
    val keyword: String,
    val type: Int = 2,
    val responseGroup: String = "small"
) : BangumiRequest<SearchSubjectBody>() {

    override val path: String = "/search/subject/${URLEncoder.encode(keyword, Charsets.UTF_8)}"
    override val responseBodyType: TypeReference<SearchSubjectBody> = jacksonTypeRef()
    override val httpMethod: String = HttpMethod.GET.name

    override fun bodyHandler(): CommonBodyHandler<SearchSubjectBody> {
        return CommonBodyHandler(this.responseBodyType, SearchSubjectBody())
    }

}