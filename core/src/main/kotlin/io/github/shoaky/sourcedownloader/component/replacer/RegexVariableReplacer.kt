package io.github.shoaky.sourcedownloader.component.replacer

import io.github.shoaky.sourcedownloader.sdk.component.VariableReplacer

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