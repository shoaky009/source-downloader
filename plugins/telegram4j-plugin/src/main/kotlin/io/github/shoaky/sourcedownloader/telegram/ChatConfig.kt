package io.github.shoaky.sourcedownloader.telegram

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

data class ChatConfig(
    @param:JsonAlias("chat-id")
    val chatId: Long,
    @param:JsonAlias("begin-date")
    val beginDate: LocalDate? = null
)