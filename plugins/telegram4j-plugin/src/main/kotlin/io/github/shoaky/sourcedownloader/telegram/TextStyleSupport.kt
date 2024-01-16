package io.github.shoaky.sourcedownloader.telegram

import telegram4j.core.`object`.MessageEntity

object TextStyleSupport {

    fun styled(content: String, entities: List<MessageEntity>, style: String = "markdown"): String {
        if (entities.isEmpty()) {
            return content
        }

        val sortedEntities = entities.sortedBy { it.offset }
        val builder = StringBuilder()
        var lastOffset = 0
        for (entity in sortedEntities) {
            val offset = entity.offset
            val length = entity.length
            if (offset > lastOffset) {
                builder.append(content.substring(lastOffset, offset))
            }
            val text = content.substring(offset, offset + length)
            val styledText = styledText(text, entity)
            builder.append(styledText)
            lastOffset = offset + length
        }
        return builder.toString()
    }

    private fun styledText(text: String, entity: MessageEntity): String {
        return when (entity.type) {
            MessageEntity.Type.BOLD -> {
                "**$text**"
            }

            MessageEntity.Type.ITALIC -> {
                "_${text}_"
            }

            MessageEntity.Type.UNDERLINE -> {
                "__${text}__"
            }

            MessageEntity.Type.STRIKETHROUGH -> {
                "~~${text}~~"
            }

            MessageEntity.Type.CODE -> {
                "`${text}`"
            }

            MessageEntity.Type.PRE -> {
                "```${text}```"
            }

            MessageEntity.Type.TEXT_URL -> {
                "[$text](${entity.url.get()})"
            }

            MessageEntity.Type.MENTION_NAME -> {
                "[$text](tg://user?id=${entity.userId})"
            }

            else -> {
                text
            }
        }
    }

}