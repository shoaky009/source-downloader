package io.github.shoaky.sourcedownloader.external.season

import org.apache.commons.lang3.CharUtils
import java.util.function.Function

data class RegexRule(
    private val regex: Regex, 
    val convert: Function<String, Int?>
) {

    fun ifMatchConvert(target: String): Int? {
        val matcher = regex.find(target)
        if (matcher != null) {
            // 如果前面有字母，那么就不算季度
            if (matcher.range.first > 0) {
                val prevChar = target[matcher.range.first - 1]
                if (CharUtils.isAsciiAlpha(prevChar)) {
                    return null
                }
            }
            return convert.apply(matcher.value)
        }
        return null
    }
}