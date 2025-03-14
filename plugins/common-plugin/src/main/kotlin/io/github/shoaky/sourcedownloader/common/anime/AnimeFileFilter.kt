package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.SourceFileFilter
import io.github.shoaky.sourcedownloader.sdk.util.TextClear
import io.github.shoaky.sourcedownloader.sdk.util.replaces
import org.slf4j.LoggerFactory
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

/**
 * 针对动画资源的过滤器，默认排除NCOP、NCED、OP、ED、映像特典、PV、CM、Fonts、Scan、Event、Lecture、Preview等文件，
 * 目标是只留下相关的视频和字幕文件
 */
object AnimeFileFilter : SourceFileFilter {

    private val replaces = listOf("-", "_", "[", "]", "(", ")", ".")
    private val mustFilterDirNames =
        setOf(
            "ncop",
            "nced",
            "trailer",
            "menu",
            "pv",
            "cm",
            "cd",
            "cds",
            "scan",
            "scans",
            "ed",
            "op",
            "fonts",
            "audio commentary",
            "preview",
            "event",
            "lecture",
            "making",
            "teaser"
        )
    private val specialDirNames = setOf(
        "sps", "sp", "special", "ncop", "nced", "menu", "pv", "cm", "cd", "cds", "scan", "scans", "extra", "特典"
    )
    private val videoExt = setOf(
        "mkv", "mp4", "webm", "avi", "flv", "mov", "wmv", "ts", "m2ts", "m4v", "rmvb", "mpg", "mpeg", "vob", "divx",
        "xvid", "3gp", "3g2", "asf", "ogm", "ogv", "rm", "ram", "swf", "f4v", "dat", "m2v", "m2p", "m2t", "m2ts", "mts",
        "mxf", "iso", "img", "bin", "cue", "nrg", "ccd", "sub", "idx",
    )
    private val archiveExt = setOf(
        "zip", "rar", "7z", "tar", "gz"
    )
    private val subtitleExt = setOf(
        "ass", "srt", "ssa", "vtt"
    )
    private val allowExt = videoExt + archiveExt + subtitleExt

    /**
     * 如果在special中的文件夹下，匹配规则可以宽松些
     */
    private val spRegexes = listOf(
        "NCOP|NCED|MENU|Fonts|Scan|Event|Lecture|Preview|特典|Other|Teaser".toRegex(RegexOption.IGNORE_CASE),
        "PV|CM|IV|Info|INFO|OP|ED|Cast| Program | MV |Making".toRegex()
    )
    private val subtitleRegex = "subtitle|字幕".toRegex(RegexOption.IGNORE_CASE)

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

    override fun test(file: SourceFile): Boolean {
        val extension = file.path.extension.lowercase()
        if (extension.isEmpty() || extension !in allowExt) {
            return false
        }

        if (extension in archiveExt) {
            val name = file.path.name
            return name.contains(subtitleRegex)
        }
        
        if (isFileInDir(file, mustFilterDirNames)) {
            return false
        }

        val isInSpecialDir = isFileInDir(file, specialDirNames)
        val regexes = if (isInSpecialDir) {
            spRegexes
        } else {
            normalRegexes
        }
        val name = textClear.input(
            file.path.nameWithoutExtension.replaces(replaces, " ")
        )
        return regexes.none {
            val containsMatchIn = it.containsMatchIn(name)
            if (log.isDebugEnabled) {
                log.debug("regex: {}, name: {}, containsMatchIn: {}", it, name, containsMatchIn)
            }
            containsMatchIn
        }
    }

    private fun isFileInDir(file: SourceFile, dirNames: Set<String>): Boolean {
        val parent = file.path.parent ?: return false
        val parenNames = parent.map { it.name.lowercase() }
        if (parenNames.isEmpty()) {
            return false
        }
        return parenNames.any { dirNames.contains(it) }
    }

    private val log = LoggerFactory.getLogger(AnimeFileFilter::class.java)
}