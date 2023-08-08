package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileTagger

/**
 * Telegram文件标签器，用于标记Telegram文件的类型
 */
object TelegramMediaTagger : FileTagger {

    override fun tag(fileContent: SourceFile): String? {
        return fileContent.attributes[TelegramSource.MEDIA_TYPE_ATTR]?.toString()
    }

}