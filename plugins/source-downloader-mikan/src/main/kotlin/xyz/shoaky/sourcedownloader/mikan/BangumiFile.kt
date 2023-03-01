package xyz.shoaky.sourcedownloader.mikan

import xyz.shoaky.sourcedownloader.mikan.parse.ParseChain
import xyz.shoaky.sourcedownloader.mikan.parse.SubjectContent
import xyz.shoaky.sourcedownloader.sdk.PatternVars
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import java.nio.file.Path
import kotlin.io.path.name

class BangumiFile(
    private val torrentFilePath: Path,
    private val bangumiVars: PatternVars,
    private val subject: SubjectContent
) : SourceFile {
    override fun downloadSavePath(downloadRootPath: Path): Path {
        return downloadRootPath.resolve(torrentFilePath)
    }

    override fun patternVars(): PatternVars {
        val patternVars = PatternVars(bangumiVars.getVars())
        val filename = torrentFilePath.last().name

        val parseChain = ParseChain()
        val apply = parseChain.apply(subject, filename)
        apply.episode?.run { patternVars.addVar("episode", this) }

        if ("1" == patternVars.getVar("season")) {
            apply.season?.run { patternVars.addVar("season", this) }
        }
        patternVars.addVar("origin-filename", filename)
        return patternVars
    }

}