package io.github.shoaky.sourcedownloader.common.dlsite

import io.github.shoaky.sourcedownloader.sdk.component.Trimmer

/**
 * 移除广告前后括号内容
 */
object DoujinTitleTrimmer : Trimmer {

    private val after = listOf("。")

    override fun trim(value: String, expectLength: Int): String {
        val br1Regex = Regex("""【[^【】]*】""")
        var result = value
        val matches = br1Regex.findAll(value).map { it.value }
        for (match in matches) {
            result = result.replace(match, "")
            if (result.length <= expectLength) {
                return result
            }
        }

        for (a in after) {
            val index = result.indexOf(a)
            if (index != -1) {
                result = result.substring(0, index)
                if (result.length <= expectLength) {
                    return result
                }
            }
        }
        return result
    }
}