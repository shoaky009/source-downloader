package io.github.shoaky.sourcedownloader.common.rss

import com.fasterxml.jackson.annotation.JsonAlias

data class RssConfig(
    val url: String,
    val tags: List<String> = emptyList(),
    val attributes: Map<String, String> = emptyMap(),
    @JsonAlias("date-format")
    val dateFormat: String? = null
)