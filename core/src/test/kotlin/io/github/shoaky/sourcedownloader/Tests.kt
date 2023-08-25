package io.github.shoaky.sourcedownloader

import io.github.shoaky.sourcedownloader.sdk.SourceItem
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

val testResourcePath = Path("src", "test", "resources")

fun sourceItem(title: String = "test", contentType: String = "",
               link: String = "http://localhost", downloadUrl: String = "http://localhost",
               attrs: Map<String, Any> = emptyMap(),
               tags: Set<String> = emptySet()
): SourceItem {
    return SourceItem(
        title,
        URI(link),
        LocalDateTime.now(),
        contentType,
        URI(downloadUrl),
        attrs,
        tags
    )
}

fun Path.createIfNotExists(): Path {
    if (this.exists()) {
        return this
    }
    this.parent.createDirectories()
    return Files.createFile(this)
}
