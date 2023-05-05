package xyz.shoaky.sourcedownloader.common.anime

import xyz.shoaky.sourcedownloader.sdk.component.SourceFileFilter
import xyz.shoaky.sourcedownloader.sdk.util.replaces
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

object AnimeFileFilter : SourceFileFilter {

    private val replaces = listOf("-", "_", "[", "]", "(", ")", ".")
    private val blockRegexes = listOf(
        " NCOP| NCED| MENU| PV| CM| Fonts".toRegex(RegexOption.IGNORE_CASE),
    )

    override fun test(t: Path): Boolean {
        val name = t.nameWithoutExtension.replaces(replaces, " ")
        return blockRegexes.none { it.containsMatchIn(name) }
    }

}