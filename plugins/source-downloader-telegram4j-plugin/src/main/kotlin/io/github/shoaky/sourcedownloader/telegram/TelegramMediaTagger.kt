package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.component.FileTagger

object TelegramMediaTagger : FileTagger {

    override fun tag(fileContent: FileContent): String? {
        return fileContent.attributes[TelegramSource.MEDIA_TYPE_ATTR]?.toString()
    }

}