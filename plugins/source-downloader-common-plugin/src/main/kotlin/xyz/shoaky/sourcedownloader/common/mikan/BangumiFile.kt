package xyz.shoaky.sourcedownloader.common.mikan

import xyz.shoaky.sourcedownloader.common.mikan.parse.SubjectContent
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PatternVariables
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import java.nio.file.Path

internal class BangumiFile(
    private val torrentFilePath: Path,
    private val subject: SubjectContent,
    private val bangumiInfo: BangumiInfo
) : SourceFile {

    override fun patternVariables(): PatternVariables {
        val variables = buildMap {
            if (bangumiInfo.season != null) {
                put("season", bangumiInfo.season)
            }
        }
        return MapPatternVariables(variables)
    }

}