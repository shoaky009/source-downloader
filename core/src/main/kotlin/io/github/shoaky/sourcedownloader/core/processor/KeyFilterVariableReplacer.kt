package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.sdk.component.VariableReplacer

class KeyFilterVariableReplacer(
    private val replacer: VariableReplacer,
    private val keys: Set<String>?,
) : VariableReplacer {

    override fun replace(key: String, value: String): String {
        if (keys == null) {
            return replacer.replace(key, value)
        }
        if (keys.contains(key)) {
            return replacer.replace(key, value)
        }
        return value
    }
}