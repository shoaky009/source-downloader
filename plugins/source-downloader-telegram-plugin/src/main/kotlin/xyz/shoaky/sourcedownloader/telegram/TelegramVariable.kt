package xyz.shoaky.sourcedownloader.telegram

import xyz.shoaky.sourcedownloader.sdk.PatternVariables

internal data class TelegramVariable(
    val chatId: Long,
    val messageId: Long,
    val chatName: String,
    val contentType: String
) : PatternVariables