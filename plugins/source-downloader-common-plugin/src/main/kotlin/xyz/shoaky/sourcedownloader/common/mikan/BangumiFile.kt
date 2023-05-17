package xyz.shoaky.sourcedownloader.common.mikan

import xyz.shoaky.sourcedownloader.common.mikan.parse.ParserChain
import xyz.shoaky.sourcedownloader.common.mikan.parse.SubjectContent
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PatternVariables
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import java.nio.file.Path
import kotlin.io.path.name

internal class BangumiFile(
    private val torrentFilePath: Path,
    private val subject: SubjectContent,
    private val bangumiInfo: BangumiInfo
) : SourceFile {

    override fun patternVariables(): PatternVariables {
        val filename = torrentFilePath.last().name

        val episodeChain = ParserChain.episodeChain()
        val result = episodeChain.apply(subject, filename)

        val length = subject.episodeLength().coerceAtLeast(2)
        val episode = result.padNumber(length) ?: return PatternVariables.EMPTY

        val variables = buildMap {
            put("episode", episode)
            if (bangumiInfo.season != null) {
                put("season", bangumiInfo.season)
            }
        }
        return MapPatternVariables(variables)
    }

}