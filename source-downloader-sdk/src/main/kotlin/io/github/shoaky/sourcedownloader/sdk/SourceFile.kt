package io.github.shoaky.sourcedownloader.sdk

import java.net.URI
import java.nio.file.Path

data class SourceFile(
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
     * The URI of the file.
     */
    val fileUri: URI? = null,
    /**
     * The tags of the file.
     */
    val tags: Set<String> = emptySet()
)