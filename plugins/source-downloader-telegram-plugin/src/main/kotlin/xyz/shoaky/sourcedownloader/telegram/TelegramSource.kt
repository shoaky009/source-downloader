package xyz.shoaky.sourcedownloader.telegram

import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.Source

class TelegramSource : Source {
    override fun fetch(): List<SourceItem> {
        return emptyList()
    }
}