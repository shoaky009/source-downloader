package xyz.shoaky.sourcedownloader.sdk.api.bangumi

import com.fasterxml.jackson.annotation.JsonProperty

data class SubjectItem(
    val id: Long,
    val name: String,
    @JsonProperty("name_cn")
    val nameCn: String,
    val url: String,
)