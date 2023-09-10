package io.github.shoaky.sourcedownloader.external.patreon

data class PatreonEntity<T>(
    val id: String,
    val type: String,
    val attributes: T,
)