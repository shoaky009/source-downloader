package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.jni.TdApi

internal data class FileMessage(
    val subject: String,
    val mimeType: String,
    val fileIds: List<Int>,
) {
    companion object {
        fun fromMessageContent(content: TdApi.MessageContent): FileMessage? {
            return when (content) {
                is TdApi.MessageDocument -> {
                    val document = content.document
                    FileMessage(
                        document.fileName,
                        document.mimeType,
                        listOf(document.document.id)
                    )
                }

                is TdApi.MessageVideo -> {
                    val video = content.video
                    FileMessage(
                        video.fileName,
                        video.mimeType,
                        listOf(video.video.id)
                    )
                }

                is TdApi.MessageAudio -> {
                    val audio = content.audio
                    FileMessage(
                        audio.fileName,
                        audio.mimeType,
                        listOf(audio.audio.id)
                    )
                }

                is TdApi.MessagePhoto -> {
                    val photo = content.photo.sizes
                    FileMessage(
                        content.caption.text,
                        "image",
                        photo.map { it.photo.id }
                    )
                }

                else -> null
            }
        }
    }
}
