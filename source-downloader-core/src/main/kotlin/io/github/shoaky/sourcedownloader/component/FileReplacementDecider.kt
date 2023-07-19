package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.SourceContent
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider

object NeverReplace : FileReplacementDecider {

    override fun isReplace(current: SourceContent, before: SourceContent?): Boolean = false

    override fun equals(other: Any?): Boolean {
        return other is NeverReplace
    }
}

object AlwaysReplace : FileReplacementDecider {

    override fun isReplace(current: SourceContent, before: SourceContent?): Boolean = true

    override fun equals(other: Any?): Boolean {
        return other is AlwaysReplace
    }
}