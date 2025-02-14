package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.component.Trimmer

object ForceTrimmer : Trimmer {

    override fun trim(value: String, expectLength: Int): String {
        return value.substring(0, expectLength)
    }
}