package xyz.shoaky.sourcedownloader.mikan

import xyz.shoaky.sourcedownloader.mikan.parse.ParserChain
import xyz.shoaky.sourcedownloader.mikan.parse.SubjectContent
import xyz.shoaky.sourcedownloader.sdk.PatternVars
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import java.nio.file.Path
import kotlin.io.path.name

internal class BangumiFile(
    private val torrentFilePath: Path,
    private val bangumiVars: PatternVars,
    private val subject: SubjectContent
) : SourceFile {

    override fun patternVars(): PatternVars {
        val patternVars = PatternVars(bangumiVars.getVars())
        val filename = torrentFilePath.last().name
        patternVars.addVar("origin-filename", filename)

        val episodeChain = ParserChain.episodeChain()
        val result = episodeChain.apply(subject, filename)

        result.padNumber(subject.episodeLength())?.run {
            patternVars.addVar("episode", this)
        }
        return patternVars
    }

}