package io.github.shoaky.sourcedownloader.component.source

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.AlwaysLatestSource
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import java.net.URI
import kotlin.io.path.Path
import kotlin.io.path.inputStream

class UriSource(
    private val uri: URI
) : AlwaysLatestSource() {

    override fun fetch(): Iterable<SourceItem> {
        if (uri.scheme == "file") {
            return Path(uri.toString().removePrefix("file://"))
                .inputStream().use {
                    Jackson.fromJson(it, jacksonTypeRef<List<SourceItem>>())
                }
        }

        return uri.toURL().openStream().use {
            Jackson.fromJson(it, jacksonTypeRef<List<SourceItem>>())
        }
    }

}