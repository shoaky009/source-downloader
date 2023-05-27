package xyz.shoaky.sourcedownloader.telegram

import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import java.nio.file.Path

class TelegramFileResolver : ItemFileResolver {
    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        TODO("Not yet implemented")
    }
}