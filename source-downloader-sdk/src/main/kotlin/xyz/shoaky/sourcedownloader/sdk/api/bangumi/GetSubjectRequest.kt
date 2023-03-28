package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import xyz.shoaky.sourcedownloader.sdk.api.HttpMethod

class GetSubjectRequest(subjectId: String) : BangumiRequest<Subject>() {
    override val path: String = "/v0/subjects/$subjectId"
    override val responseBodyType: TypeReference<Subject> = jacksonTypeRef()
    override val httpMethod: HttpMethod = HttpMethod.GET
}