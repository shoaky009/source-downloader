package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider

object AnimeReplacementDecider : FileReplacementDecider {

    private val versionRegex = Regex("\\[v\\d+]", RegexOption.IGNORE_CASE)

    override fun isReplace(current: ItemContent, before: ItemContent?): Boolean {
        val title = current.sourceItem.title
        // 有水印的不要
        if (title.contains("bilibili", true) || title.contains("仅限港澳台地区", true)) {
            return false
        }

        return version(current, before)
    }

    private fun version(current: ItemContent, before: ItemContent?): Boolean {
        val title = current.sourceItem.title
        return versionRegex.find(title)?.let { true } ?: false
    }
}