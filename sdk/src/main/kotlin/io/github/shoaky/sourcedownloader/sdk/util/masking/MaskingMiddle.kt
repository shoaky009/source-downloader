package io.github.shoaky.sourcedownloader.sdk.util.masking

import com.google.common.base.Strings
import org.apache.commons.lang3.StringUtils
import kotlin.math.min

/**
 * 我是 -> 我*
 * 我是谁 -> 我*谁
 * 我是是谁 -> 我是*谁
 * 我是是是谁 -> 我是*是谁
 * 我是是是是谁 -> 我是**是谁
 * 如果*的长度超过10个 会以10个来显示多余的省略
 */
class MaskingMiddle : StringMasking {

    override fun mask(data: String): String {
        if (StringUtils.isBlank(data) || data.length < 2) {
            return data
        }
        val length = data.length
        val mid = Math.floorDiv(length, 2)
        val prefixEnd = Math.floorDiv(mid, 2) + 1
        val prefix = data.substring(0, prefixEnd)
        val end = length - mid
        var suffixBegin = mid + Math.floorDiv(end, 2)
        if (prefixEnd == suffixBegin) {
            suffixBegin++
        }
        val suffix = data.substring(suffixBegin, length)
        val maskingCount = min((suffixBegin - prefixEnd).toDouble(), MAX_MASKING.toDouble())
            .toInt()
        val repeat = Strings.repeat("*", maskingCount)
        return prefix + repeat + suffix
    }

    companion object {

        private const val MAX_MASKING = 10
    }
}
