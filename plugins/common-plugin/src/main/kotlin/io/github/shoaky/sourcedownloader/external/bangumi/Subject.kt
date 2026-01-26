package io.github.shoaky.sourcedownloader.external.bangumi

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class Subject(
    val id: Long,
    val name: String,
    @param:JsonProperty("name_cn")
    val nameCn: String?,
    val date: LocalDate?,
    @param:JsonProperty("total_episodes")
    val totalEpisodes: Int?,
    val nsfw: Boolean = false
)
