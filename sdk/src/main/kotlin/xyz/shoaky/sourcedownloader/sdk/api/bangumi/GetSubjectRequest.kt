package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.http.HttpMethod

class GetSubjectRequest(subjectId: String) : BangumiRequest<Subject>() {
    override val path: String = "/v0/subjects/$subjectId"
    override val responseBodyType: TypeReference<Subject> = object : TypeReference<Subject>() {}
    override val httpMethod: HttpMethod = HttpMethod.GET
}