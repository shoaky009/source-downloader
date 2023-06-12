package io.github.shoaky.sourcedownloader.external.anilist

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.api.HttpMethod

abstract class AnilistRequest<T : Any> : BaseRequest<T>() {

    init {
        setHeader(HttpHeaders.ACCEPT, MediaType.JSON_UTF_8)
    }

    override val mediaType: MediaType = MediaType.JSON_UTF_8
    override val path: String = "/"
    override val httpMethod: HttpMethod = HttpMethod.POST

}

class Search(
    search: String,
    page: Int = 1,
    perPage: Int = 10,
    type: String = "ANIME",
    val query: String = """
        query (${'$'}id: Int, ${'$'}search: String, ${'$'}page: Int, ${'$'}perPage: Int) {
          Page (page: ${'$'}page, perPage: ${'$'}perPage) {
            pageInfo {
              total
            }
            media (id: ${'$'}id, search: ${'$'}search, type: $type) {
              id
              title {
                romaji
                native
              }
            }
          }
        }
    """.trimIndent(),
    val variables: Map<String, Any> = mapOf(
        "search" to search,
        "page" to page,
        "perPage" to perPage,
    )
) : AnilistRequest<GraphQLResponse<PageResponse>>() {

    override val responseBodyType = jacksonTypeRef<GraphQLResponse<PageResponse>>()

    override val path: String = "/"

}