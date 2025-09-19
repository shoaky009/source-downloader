package io.github.shoaky.sourcedownloader.telegram

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

data class ChatConfig(
    @field:JsonAlias("chat-id")
    val chatId: Long,
    @field:JsonAlias("begin-date")
    val beginDate: LocalDate? = null
)