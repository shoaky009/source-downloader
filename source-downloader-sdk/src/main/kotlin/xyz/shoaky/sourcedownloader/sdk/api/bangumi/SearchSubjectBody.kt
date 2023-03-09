package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import com.fasterxml.jackson.annotation.JsonProperty

data class SearchSubjectBody(
    val total: Int,
    val data: List<Item>
) : Iterable<SearchSubjectBody.Item> {
    data class Item(
        val id: Long,
        val name: String,
        @JsonProperty("name_cn")
        val nameCn: String,
        val image: String
    )

    override fun iterator(): Iterator<Item> {
        return data.iterator()
    }
}