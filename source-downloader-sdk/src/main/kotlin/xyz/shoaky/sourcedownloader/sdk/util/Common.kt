package xyz.shoaky.sourcedownloader.sdk.util

import java.util.*


fun String.find(vararg regexes: Regex): String? {
    for (regex in regexes) {
        val match = regex.find(this)
        if (match != null) {
            return match.value
        }
    }
    return null
}

fun String.replaces(replaces: List<String>, to: String, ignoreCase: Boolean = true): String {
    var result = this
    for (replace in replaces) {
        result = result.replace(replace, to, ignoreCase)
    }
    return result
}

fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(this.toByteArray())
}