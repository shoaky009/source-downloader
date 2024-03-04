package io.github.shoaky.sourcedownloader.component.replacer

import io.github.shoaky.sourcedownloader.sdk.component.VariableReplacer

object FullWidthReplacer : VariableReplacer {

    private val alCodeRange = 65281..65374
    private const val AL_CODE_OFFSET: Int = 65248
    private val others = mapOf(
        '（' to '(',
        '）' to ')',
        '【' to '[',
        '】' to ']',
        '～' to '~',
        '！' to '!',
        '？' to '?',
    )

    override fun replace(key: String, value: String): String {
        val stringBuilder = StringBuilder()
        val charArray = value.toCharArray()
        for (char in charArray) {
            val code = char.code
            when {
                others.containsKey(char) -> stringBuilder.append(others[char])
                code in alCodeRange -> stringBuilder.append((code - AL_CODE_OFFSET).toChar())
                else -> stringBuilder.append(char)
            }
        }
        return stringBuilder.toString()
    }

}