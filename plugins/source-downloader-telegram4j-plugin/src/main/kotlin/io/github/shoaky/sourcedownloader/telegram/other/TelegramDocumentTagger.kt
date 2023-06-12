package io.github.shoaky.sourcedownloader.telegram.other

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.component.FileTagger

object TelegramDocumentTagger : FileTagger {
    override fun tag(fileContent: FileContent): String? {
        return fileContent.attributes["telegramDocumentType"]?.toString()
    }

}