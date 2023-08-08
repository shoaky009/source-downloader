package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider

/**
 * 永远不替换文件
 */
object NeverReplace : FileReplacementDecider {

    override fun isReplace(current: ItemContent, before: ItemContent?): Boolean = false

    override fun equals(other: Any?): Boolean {
        return other is NeverReplace
    }
}

/**
 * 永远替换文件
 */
object AlwaysReplace : FileReplacementDecider {

    override fun isReplace(current: ItemContent, before: ItemContent?): Boolean = true

    override fun equals(other: Any?): Boolean {
        return other is AlwaysReplace
    }
}