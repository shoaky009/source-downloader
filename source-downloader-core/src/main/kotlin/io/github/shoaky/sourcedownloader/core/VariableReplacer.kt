package io.github.shoaky.sourcedownloader.core

interface VariableReplacer {

    fun replace(key: String, value: String): String

}

class RegexVariableReplacer(
    private val regex: Regex,
    private val replacement: String,
) : VariableReplacer {

    override fun replace(key: String, value: String): String {
        return regex.replace(value, replacement)
    }

    override fun toString(): String {
        return this.regex.toString() + " -> " + this.replacement
    }
}

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