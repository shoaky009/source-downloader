package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.io.path.Path

class HtmlFileResolver(
    private val cssSelector: String,
    private val extractAttribute: String,
    private val directMode: Boolean = false
) : ItemFileResolver {

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val document = Jsoup.newSession().url(sourceItem.downloadUri.toURL()).get()
        if (log.isDebugEnabled) {
            log.debug("URL response: {}", document)
        }
        return document.select(cssSelector).mapIndexed { index, element ->
            val uri = URI(element.attr(extractAttribute))
            val filename = resolveFilename(uri, sourceItem, index)
            if (directMode) {
                SourceFile(
                    Path(filename),
                    data = uri.toURL().openStream(),
                )
            } else {
                SourceFile(
                    Path(filename),
                    downloadUri = uri
                )
            }
        }
    }

    private fun resolveFilename(uri: URI, sourceItem: SourceItem, index: Int): String {
        val last = uri.path.split("/").last()
        if (last.contains(".")) {
            return last
        }
        return "${sourceItem.hashing()}_${index}.html"
    }

    companion object {

        private val log = LoggerFactory.getLogger(HtmlFileResolver::class.java)
    }
}