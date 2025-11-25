package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider

/**
 * 针对动画资源的替换决策器，替换Bilibili的源，如果有版本号例如v2v3会进行替换
 */
object AnimeReplacementDecider : FileReplacementDecider {

    private val versionRegex = Regex("\\[\\d{0,4}(?i)v(\\d+)]")

    override fun shouldReplace(current: ItemContent, before: ItemContent?, existingFile: SourceFile): Boolean {
        val title = current.sourceItem.title
        val currentItemRating = Rating.from(title)
        if (before == null) {
            return currentItemRating.getScore() > 0
        }

        val beforeItemRating = Rating.from(before.sourceItem.title)
        if (beforeItemRating.prerelease && currentItemRating.prerelease) {
            return false
        }
        if (currentItemRating.bilibili && beforeItemRating.bilibili.not()) {
            return false
        }
        return currentItemRating.getScore() > beforeItemRating.getScore()
    }

    private fun isBilibili(text: String): Boolean {
        listOf("bilibili", "仅限港澳台地区", "仅限台湾地区", "b-global").forEach {
            if (text.contains(it, true)) {
                return true
            }
        }
        return false
    }

    private fun isPrerelease(text: String): Boolean {
        return text.contains("偷跑") || text.contains("先行")
    }

    private data class Rating(
        val bilibili: Boolean,
        val prerelease: Boolean,
        val version: Int? = null,
    ) {

        fun getScore(): Int {
            var score = 0
            if (version != null) {
                score += version
            }
            // bilibili的有可能会有版本号
            if (bilibili) {
                score -= 1
            }
            // 偷跑的版本都不替换
            if (prerelease) {
                score = -1
            }
            return score

        }

        companion object {

            fun from(text: String): Rating {
                val bilibili = isBilibili(text)
                val prerelease = isPrerelease(text)
                val version = versionRegex.find(text)?.groupValues?.lastOrNull()?.toIntOrNull()
                return Rating(bilibili, prerelease, version)
            }
        }
    }


}