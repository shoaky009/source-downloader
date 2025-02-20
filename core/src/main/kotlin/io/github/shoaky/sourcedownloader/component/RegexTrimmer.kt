package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.component.Trimmer

class RegexTrimmer(
    val regex: Regex
) : Trimmer {

    override fun trim(value: String, expectLength: Int): String {
        return value.replace(regex, "")
    }
}