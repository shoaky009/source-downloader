package io.github.shoaky.sourcedownloader.component.replacer

import io.github.shoaky.sourcedownloader.sdk.component.VariableReplacer

object WindowsPathReplacer : VariableReplacer {

    private val charReplacements: Map<Char, Char> = mapOf(
        '<' to '＜',
        '>' to '＞',
        ':' to '：',
        '"' to '＂',
        '/' to '／',
        '\\' to '＼',
        '|' to '｜',
        '?' to '？',
        '*' to '＊'
    )

    override fun replace(key: String, value: String): String {
        val stringBuilder = StringBuilder()
        val charArray = value.toCharArray()
        for (char in charArray) {
            val c = charReplacements.getOrDefault(char, char)
            stringBuilder.append(c)
        }
        return stringBuilder.toString()
    }

}