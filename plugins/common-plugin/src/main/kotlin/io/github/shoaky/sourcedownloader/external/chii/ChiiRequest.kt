package io.github.shoaky.sourcedownloader.external.chii

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.external.anilist.GraphQLResponse
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.http.HttpMethod
import java.time.LocalDate

class SubjectQueryChiiRequest(
    search: String,
    val operationName: String = "SubjectSearch",
    val query: String = """
        query SubjectSearch(${'$'}q: String, ${'$'}type: String) {
          querySubjectSearch(q: ${'$'}q, type: ${'$'}type) {
            result {
              ... on Subject {
                id
                name
                nameCN
                nsfw
                date
              }
            }
          }
	    }
    """.trimIndent(),
    val variables: Map<String, Any> = mapOf(
        "q" to search,
        "type" to "anime",
    )
) : BaseRequest<GraphQLResponse<SubjectSearchResponse>>() {

    override val responseBodyType: TypeReference<GraphQLResponse<SubjectSearchResponse>> = jacksonTypeRef()
    override val mediaType: MediaType = MediaType.JSON_UTF_8
    override val path: String = "/graphql"
    override val httpMethod: String = HttpMethod.POST.name
}

data class SubjectSearchResponse(
    val querySubjectSearch: QuerySubjectSearch
)

data class QuerySubjectSearch(
    val result: List<SubjectResultItem>
)

data class SubjectResultItem(
    val id: String,
    val name: String,
    @JsonProperty("nameCN")
    val nameCn: String,
)