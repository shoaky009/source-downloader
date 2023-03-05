package xyz.shoaky.sourcedownloader

import xyz.shoaky.sourcedownloader.sdk.SourceItem
import java.net.URL
import java.time.LocalDateTime

object Tests {

}

fun sourceItem(name: String = "test", contentType: String = "",
               link: String = "http://localhost", downloadUrl: String = "http://localhost"
): SourceItem {
    return SourceItem(
        name,
        URL(link),
        LocalDateTime.now(),
        contentType,
        URL(downloadUrl)
    )
}