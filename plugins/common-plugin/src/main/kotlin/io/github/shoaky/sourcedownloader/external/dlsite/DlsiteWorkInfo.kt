package io.github.shoaky.sourcedownloader.external.dlsite

import io.github.shoaky.sourcedownloader.sdk.PatternVariables

data class DlsiteWorkInfo(
    val dlsiteId: String,
    val title: String? = null,
    val releaseDate: String? = null,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val seriesName: String? = null,
    val maker: String? = null,
    // val scenario: String? = null,
    // val illustration: String? = null,
    // val voiceActor: String? = null,
    val productFormat: String? = null,
    // manga
    val author: String? = null,

    // more....
) : PatternVariables