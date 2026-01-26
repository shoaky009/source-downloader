package io.github.shoaky.sourcedownloader.external.transmission

import com.fasterxml.jackson.annotation.JsonProperty
import java.nio.file.Path

data class Session(
    @param:JsonProperty("download-dir")
    val downloadPath: Path,
    val version: String
)
