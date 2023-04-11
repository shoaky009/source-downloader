package xyz.shoaky.sourcedownloader.mikan

import xyz.shoaky.sourcedownloader.mikan.parse.ParserChain
import xyz.shoaky.sourcedownloader.mikan.parse.SubjectContent
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PatternVariables
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import java.nio.file.Path
import kotlin.io.path.name

internal class BangumiFile(
    private val torrentFilePath: Path,
    private val subject: SubjectContent
) : SourceFile {

    override fun patternVariables(): PatternVariables {
        val filename = torrentFilePath.last().name

        val episodeChain = ParserChain.episodeChain()
        val result = episodeChain.apply(subject, filename)

        val padNumber = result.padNumber(subject.episodeLength()) ?: return PatternVariables.EMPTY
        return MapPatternVariables(
            mapOf("episode" to padNumber)
        )
    }

}