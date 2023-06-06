package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.jni.TdApi

internal data class FileMessage(
    val subject: String,
    val mimeType: String,
    val fileId: Int,
) {
    companion object {
        fun fromMessageContent(message: TdApi.Message): FileMessage? {
            return when (val content = message.content) {
                is TdApi.MessageDocument -> {
                    val document = content.document
                    FileMessage(
                        document.fileName,
                        document.mimeType,
                        document.document.id
                    )
                }

                is TdApi.MessageVideo -> {
                    val video = content.video
                    FileMessage(
                        video.fileName,
                        video.mimeType,
                        video.video.id
                    )
                }

                is TdApi.MessageAudio -> {
                    val audio = content.audio
                    FileMessage(
                        audio.fileName,
                        audio.mimeType,
                        audio.audio.id
                    )
                }

                is TdApi.MessagePhoto -> {
                    val photo = content.photo.sizes
                    val chatId = message.chatId
                    val messageId = message.id
                    FileMessage(
                        content.caption.text.ifBlank { "$chatId-$messageId" },
                        "image/jpg",
                        photo.maxBy { it.photo.expectedSize }.photo.id
                    )
                }

                else -> null
            }
        }
    }
}
