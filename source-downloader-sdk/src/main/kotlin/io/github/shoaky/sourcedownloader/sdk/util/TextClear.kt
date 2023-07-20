package io.github.shoaky.sourcedownloader.sdk.util

class TextClear(
    private val replaces: Map<Regex, String>,
) {

    fun input(text: String): String {
        var res = text
        replaces.forEach {
            res = text.replace(it.key, it.value)
        }
        return res
    }
}