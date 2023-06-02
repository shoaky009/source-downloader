package xyz.shoaky.sourcedownloader.telegram.other

import xyz.shoaky.sourcedownloader.sdk.FileContent
import xyz.shoaky.sourcedownloader.sdk.component.FileTagger

object TelegramDocumentTagger : FileTagger {
    override fun tag(fileContent: FileContent): String? {
        return fileContent.attributes["telegramDocumentType"]?.toString()
    }

}