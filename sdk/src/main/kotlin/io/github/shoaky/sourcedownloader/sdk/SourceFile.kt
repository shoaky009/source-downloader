package io.github.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.InputStream
import java.net.URI
import java.nio.file.Path

data class SourceFile @JvmOverloads constructor(
    /**
     * The path of the file.
     * From [ItemFileResolver], it's a relative path.
     * In the context of submitting a task, it's an absolute path.
     */
    val path: Path,
    /**
     * The attributes of the file.
     */
    val attrs: Map<String, Any> = emptyMap(),
    /**
     * The URI of the file, provided for use by a downloader, but the specifics depend on the implementation of the downloader.
     */
    val fileUri: URI? = null,
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