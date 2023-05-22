package xyz.shoaky.sourcedownloader

import xyz.shoaky.sourcedownloader.sdk.SourceItem
import java.net.URI
import java.time.LocalDateTime
import kotlin.io.path.Path

object Tests {

}

val testResourcePath = Path("src", "test", "resources")

fun sourceItem(title: String = "test", contentType: String = "",
               link: String = "http://localhost", downloadUrl: String = "http://localhost"
): SourceItem {
    return SourceItem(
        title,
        URI(link),
        LocalDateTime.now(),
        contentType,
        URI(downloadUrl)
    )
}