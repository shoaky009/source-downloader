package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.component.FileContentFilter
import io.github.shoaky.sourcedownloader.sdk.util.replaces
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

object AnimeFileFilter : FileContentFilter {

    private val replaces = listOf("-", "_", "[", "]", "(", ")", ".")

    /**
     * 如果在special中的文件夹下，匹配规则可以宽松些
     */
    private val spRegexes = listOf(
        "NCOP|NCED|MENU|PV|CM|Fonts|IV|Scan|Event|Lecture|Preview".toRegex(RegexOption.IGNORE_CASE),
    )

    private val normalRegexes = listOf(
        "NCED|NCOP|\\s+MENU|\\s+PV|\\s+CM|\\s+Fonts|^MENU(\\d+)?$|^PV(\\d+)?\$".toRegex(RegexOption.IGNORE_CASE),
    )

    private val specialDirNames = setOf(
        "sps", "sp", "special", "ncop", "nced", "menu", "pv", "cm", "cd", "cds", "scan", "scans", "extra"
    )

    override fun test(content: FileContent): Boolean {
        val path = content.fileDownloadPath
        val isInSpecialDir = isInSpecialDir(path)
        val regexes = if (isInSpecialDir) {
            spRegexes
        } else {
            normalRegexes
        }
        val name = path.nameWithoutExtension.replaces(replaces, " ")
        return regexes.none { it.containsMatchIn(name) }
    }


    private fun isInSpecialDir(path: Path): Boolean {
        val parenName = path.parent?.name
        return parenName != null && specialDirNames.contains(parenName.lowercase())
    }

}