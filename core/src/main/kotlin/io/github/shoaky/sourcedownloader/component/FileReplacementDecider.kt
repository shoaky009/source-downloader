package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider

/**
 * 永远不替换文件
 */
object NeverReplace : FileReplacementDecider {

    override fun shouldReplace(current: ItemContent, before: ItemContent?, existingFile: SourceFile): Boolean = false

    override fun equals(other: Any?): Boolean {
        return other is NeverReplace
    }
}

/**
 * 永远替换文件
 */
object AlwaysReplace : FileReplacementDecider {

    override fun shouldReplace(current: ItemContent, before: ItemContent?, existingFile: SourceFile): Boolean = true

    override fun equals(other: Any?): Boolean {
        return other is AlwaysReplace
    }
}