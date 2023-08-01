package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider

object AnimeReplacementDecider : FileReplacementDecider {

    private val versionRegex = Regex("\\[v\\d+]", RegexOption.IGNORE_CASE)

    override fun isReplace(current: ItemContent, before: ItemContent?): Boolean {
        val title = current.sourceItem.title
        // 有水印的不要
        if (isBilibili(title)) {
            return false
        }

        if (replaceBilibili(before)) {
            return true
        }

        return version(current, before)
    }

    private fun replaceBilibili(before: ItemContent?): Boolean {
        if (before == null) {
            return false
        }
        val title = before.sourceItem.title
        return isBilibili(title)
    }

    private fun version(current: ItemContent, before: ItemContent?): Boolean {
        // enhance, before version greater than current version don't replace
        val title = current.sourceItem.title
        return versionRegex.find(title)?.let { true } ?: false
    }

    private fun isBilibili(text: String): Boolean {
        listOf("bilibili", "仅限港澳台地区", "仅限台湾地区", "b-global").forEach {
            if (text.contains(it, true)) {
                return true
            }
        }
        return false
    }
}