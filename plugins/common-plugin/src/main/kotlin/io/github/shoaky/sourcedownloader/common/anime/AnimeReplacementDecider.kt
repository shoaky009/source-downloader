package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider

/**
 * 针对动画资源的替换决策器，替换Bilibili的源，如果有版本号例如v2v3会进行替换
 */
object AnimeReplacementDecider : FileReplacementDecider {

    private val versionRegex = Regex("\\[v\\d+]", RegexOption.IGNORE_CASE)

    override fun isReplace(current: ItemContent, before: ItemContent?, existingFile: SourceFile): Boolean {
        val title = current.sourceItem.title
        val isBilibili = isBilibili(title)
        val hasVersion = hasVersion(current, before)
        if (isBilibili && hasVersion.not()) {
            return false
        }

        if (replaceBilibili(before)) {
            return true
        }

        if (before != null && isBilibili) {
            return false
        }
        return hasVersion(current, before)
    }

    private fun replaceBilibili(before: ItemContent?): Boolean {
        if (before == null) {
            return false
        }
        val title = before.sourceItem.title
        return isBilibili(title)
    }

    private fun hasVersion(current: ItemContent, before: ItemContent?): Boolean {
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