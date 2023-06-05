package xyz.shoaky.sourcedownloader

import xyz.shoaky.sourcedownloader.sdk.SourceItem
import java.net.URI
import java.time.LocalDateTime


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