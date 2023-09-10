package io.github.shoaky.sourcedownloader.external.patreon

import com.fasterxml.jackson.databind.JsonNode

data class PatreonResponse<T>(
    val data: T,
    val included: List<JsonNode> = emptyList(),
)