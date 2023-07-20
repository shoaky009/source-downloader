package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider

object NeverReplace : FileReplacementDecider {

    override fun isReplace(current: ItemContent, before: ItemContent?): Boolean = false

    override fun equals(other: Any?): Boolean {
        return other is NeverReplace
    }
}

object AlwaysReplace : FileReplacementDecider {

    override fun isReplace(current: ItemContent, before: ItemContent?): Boolean = true

    override fun equals(other: Any?): Boolean {
        return other is AlwaysReplace
    }
}