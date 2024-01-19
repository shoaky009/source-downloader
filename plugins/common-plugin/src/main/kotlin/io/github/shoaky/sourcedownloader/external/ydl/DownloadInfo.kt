package io.github.shoaky.sourcedownloader.external.ydl

import com.fasterxml.jackson.annotation.JsonProperty

data class DownloadInfo(
    val uid: String,
    val url: String,
    val title: String,
    val type: String,
    @JsonProperty("percent_complete")
    val percentComplete: Double
)