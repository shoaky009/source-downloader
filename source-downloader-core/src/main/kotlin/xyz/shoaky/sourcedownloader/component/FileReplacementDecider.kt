package xyz.shoaky.sourcedownloader.component

import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.FileReplacementDecider


object NeverReplace : FileReplacementDecider {

    override fun isReplace(current: SourceContent, before: SourceContent?): Boolean = false

    override fun equals(other: Any?): Boolean {
        return other is NeverReplace
    }
}

