package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import com.fasterxml.jackson.annotation.JsonProperty

data class SubjectV0Item(
    val id: Long,
    val name: String,
    @JsonProperty("name_cn")
    val nameCn: String,
    val image: String
)