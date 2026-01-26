package io.github.shoaky.sourcedownloader.external.bangumi

import com.fasterxml.jackson.annotation.JsonProperty

data class SubjectV0Item(
    val id: Long,
    val name: String,
    @param:JsonProperty("name_cn")
    val nameCn: String,
    val image: String
)