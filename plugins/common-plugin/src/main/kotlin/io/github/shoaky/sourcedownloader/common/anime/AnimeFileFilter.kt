package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.component.FileContentFilter
import io.github.shoaky.sourcedownloader.sdk.util.TextClear
import io.github.shoaky.sourcedownloader.sdk.util.replaces
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

/**
 * 针对动画资源的过滤器，默认排除NCOP、NCED、OP、ED、映像特典、PV、CM、Fonts、Scan、Event、Lecture、Preview等文件
 */
object AnimeFileFilter : FileContentFilter {

    private val replaces = listOf("-", "_", "[", "]", "(", ")", ".")

    private val specialDirNames = setOf(
        "sps", "sp", "special", "ncop", "nced", "menu", "pv", "cm", "cd", "cds", "scan", "scans", "extra", "特典"
    )

    /**
     * 如果在special中的文件夹下，匹配规则可以宽松些
     */
    private val spRegexes = listOf(
        "NCOP|NCED|MENU|Fonts|Scan|Event|Lecture|Preview|特典|Other".toRegex(RegexOption.IGNORE_CASE),
        "PV|CM|IV|Info|INFO|OP|ED|Cast| Program | MV |Making".toRegex()
    )

    // SPECIAL CD
    // SP
    // LOGO
    private val normalRegexes = listOf(
        Regex(
            "preview|fonts|nced|ncop|font|audio commentary|trailer",
            RegexOption.IGNORE_CASE
        ),
        Regex("Info(\\d+)|ed(\\d+)|op(\\d+)|event(\\d+)", RegexOption.IGNORE_CASE),
        "\\b\\s+OP\\b|\\b\\s+ED\\b|\\s+MENU|\\s+PV|\\s+CM|\\s+Fonts|^MENU(\\d+)?$|^PV(\\d+)?\$|映像特典|^MENU ".toRegex(
            RegexOption.IGNORE_CASE
        ),
    )

    private val textClear = TextClear(
        mapOf(
            Regex("\\b[A-Fa-f0-9]{8}\\b", RegexOption.IGNORE_CASE) to "",
        )
    )

    override fun test(content: FileContent): Boolean {
        val path = content.fileDownloadPath
        val isInSpecialDir = isInSpecialDir(path)
        val regexes = if (isInSpecialDir) {
            spRegexes
        } else {
            normalRegexes
        }
        val name = textClear.input(
            path.nameWithoutExtension.replaces(replaces, " ")
        )
        return regexes.none {
            val containsMatchIn = it.containsMatchIn(name)
            if (log.isDebugEnabled) {
                log.debug("regex: {}, name: {}, containsMatchIn: {}", it, name, containsMatchIn)
            }
            containsMatchIn
        }
    }

    private fun isInSpecialDir(path: Path): Boolean {
        val parenName = path.parent?.name
        return parenName != null && specialDirNames.contains(parenName.lowercase())
    }

    private val log = LoggerFactory.getLogger(AnimeFileFilter::class.java)
}