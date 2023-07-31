package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileTagger

object TelegramMediaTagger : FileTagger {

    override fun tag(fileContent: SourceFile): String? {
        return fileContent.attributes[TelegramSource.MEDIA_TYPE_ATTR]?.toString()
    }

}