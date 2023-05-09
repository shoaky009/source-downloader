package xyz.shoaky.sourcedownloader.external.bangumi

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class Subject(
    val id: Long,
    val name: String,
    @JsonProperty("name_cn")
    val nameCn: String,
    val date: LocalDate,
    @JsonProperty("total_episodes")
    val totalEpisodes: Int,
    val nsfw: Boolean = false
)
