package xyz.shoaky.sourcedownloader.common.mikan

import xyz.shoaky.sourcedownloader.sdk.FileVariable
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PatternVariables

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