package xyz.shoaky.sourcedownloader.common.fanbox

import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.Source

class FanboxSource(
    val sessionId: String,
) : Source {
    override fun fetch(): List<SourceItem> {
        // Http.client.send()

        return emptyList()
    }
}

