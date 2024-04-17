package io.github.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import java.nio.file.Path

data class ProcessorInfo(
    val name: String,
    @JsonSerialize(using = ToStringSerializer::class)
    val downloadPath: Path,
    @JsonSerialize(using = ToStringSerializer::class)
    val sourceSavePath: Path,
    val tags: List<String> = emptyList(),
    val category: String? = null,
    // val components: Map<String, Any>
)