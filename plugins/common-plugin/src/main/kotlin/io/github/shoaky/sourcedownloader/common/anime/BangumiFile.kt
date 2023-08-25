package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.FileVariable
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables

internal class BangumiFile(
    private val bangumiInfo: BangumiInfo
) : FileVariable {

    override fun patternVariables(): PatternVariables {
        val variables = buildMap {
            if (bangumiInfo.season != null) {
                put("season", bangumiInfo.season)
            }
        }
        return MapPatternVariables(variables)
    }

}