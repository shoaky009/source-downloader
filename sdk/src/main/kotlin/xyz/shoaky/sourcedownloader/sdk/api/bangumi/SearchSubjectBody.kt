package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import com.fasterxml.jackson.annotation.JsonProperty

data class SearchSubjectBody(val results: Int,
                             val list: List<Item>
) : Iterable<SearchSubjectBody.Item> {
    data class Item(val id: Long, val name: String,
                    @JsonProperty("name_cn")
                    val nameCn: String, val url: String)

    override fun iterator(): Iterator<Item> {
        return list.iterator()
    }
}