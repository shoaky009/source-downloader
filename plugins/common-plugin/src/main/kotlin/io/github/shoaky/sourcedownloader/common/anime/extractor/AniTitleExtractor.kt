package io.github.shoaky.sourcedownloader.common.anime.extractor

/**
 * ANI字幕组的命名风格到集数之间的内容，再判断是否需要提取罗马或标题
 */
object AniTitleExtractor : Extractor {

    private const val GROUP = "[ANi]"
    private val EPISODE_REGEX = Regex(" - \\d+(\\.\\d+)? \\[.*]")
    private const val INVALID_INDEX = -1
    private const val FIRST_SEPARATE = " - "
    private const val SECOND_SEPARATE = " / "

    override fun extract(raw: String): List<String>? {
        val groupIndex = raw.indexOf(GROUP, ignoreCase = true)
        if (groupIndex == INVALID_INDEX) {
            return null
        }
        val startIndex = groupIndex + GROUP.length
        val endIndex = EPISODE_REGEX.find(raw)?.range?.start ?: INVALID_INDEX
        if (endIndex == INVALID_INDEX) {
            return null
        }
        val title = raw.substring(startIndex, endIndex)
        var multiTitle = title.split(FIRST_SEPARATE)
        if (multiTitle.size == 1) {
            multiTitle = title.split(SECOND_SEPARATE)
        }
        if (multiTitle.isEmpty()) {
            return null
        }
        if (multiTitle.size == 1) {
            return listOf(multiTitle.first())
        }
        return multiTitle
    }

}