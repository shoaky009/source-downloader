package io.github.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import java.io.InputStream
import java.net.URI
import java.nio.file.Path

data class SourceFile @JvmOverloads constructor(
    /**
     * The path of the file.
     * From [ItemFileResolver], it's a relative path.
     * In the context of submitting a task, it's an absolute path.
     */
    @JsonSerialize(using = ToStringSerializer::class)
    val path: Path,
    /**
     * The attributes of the file.
     */
    val attrs: Map<String, Any> = emptyMap(),
    /**
     * The URI of the file, provided for use by a downloader, but the specifics depend on the implementation of the downloader.
     */
    @JsonAlias("fileUri")
    val downloadUri: URI? = null,
    /**
     * The tags of the file.
     */
    val tags: Set<String> = emptySet(),
    /**
     * The data of the file.
     * Will not serialize, and the downloading will be taken over by the 'core' instead of a custom downloader.
     */
    @JsonIgnore
    val data: InputStream? = null
)