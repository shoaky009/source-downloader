package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider

object AnimeReplacementDecider : FileReplacementDecider {

    private val versionRegex = Regex("\\[v\\d+]", RegexOption.IGNORE_CASE)

    override fun isReplace(current: ItemContent, before: ItemContent?): Boolean {
        // 简单写一个 后续继续补充
        return version(current, before)
    }

    private fun version(current: ItemContent, before: ItemContent?): Boolean {
        val title = current.sourceItem.title
        return versionRegex.find(title)?.let { true } ?: false
    }
}