package xyz.shoaky.sourcedownloader.mikan

import xyz.shoaky.sourcedownloader.mikan.parse.ParserChain
import xyz.shoaky.sourcedownloader.mikan.parse.SubjectContent
import xyz.shoaky.sourcedownloader.sdk.PatternVariables
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import java.nio.file.Path
import kotlin.io.path.name

internal class BangumiFile(
    private val torrentFilePath: Path,
    private val bangumiInfo: BangumiInfo,
    private val subject: SubjectContent
) : SourceFile {

    override fun patternVariables(): PatternVariables {
        val filename = torrentFilePath.last().name

        val episodeChain = ParserChain.episodeChain()
        val result = episodeChain.apply(subject, filename)

        val copy = bangumiInfo.copy()
        result.padNumber(subject.episodeLength())?.run {
            copy.episode = this
        }
        return copy
    }

}