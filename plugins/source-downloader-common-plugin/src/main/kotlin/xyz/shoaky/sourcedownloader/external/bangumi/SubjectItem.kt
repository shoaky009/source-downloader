package xyz.shoaky.sourcedownloader.external.bangumi

import com.fasterxml.jackson.annotation.JsonProperty

data class SubjectItem(
    val id: Long,
    val name: String,
    @JsonProperty("name_cn")
    val nameCn: String,
    val url: String,
)