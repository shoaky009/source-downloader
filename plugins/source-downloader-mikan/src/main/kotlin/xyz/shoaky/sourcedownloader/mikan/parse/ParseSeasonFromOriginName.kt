package xyz.shoaky.sourcedownloader.mikan.parse

import java.util.regex.Pattern

internal class ParseSeasonFromOriginName : SeasonEpisodeParser {

    override val name: String = "SeasonFromName"

    override fun apply(subjectContent: SubjectContent, filename: String): Result {
        val subject = subjectContent.subject
        val originName = subject.name
        val matcher = numberPattern.matcher(originName)
        val matched = mutableListOf<Matched>()
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            matched.add(Matched(originName, matcher.group(), start, end))
        }
        val season: Int = matched.lastOrNull()?.getNumber() ?: 1
        return Result(season, null)
    }

    companion object {
        internal val numberPattern: Pattern = Pattern.compile(
            "\\d+|一|二|三|四|五|六|七|八|九|十|" +
                    "II|III|IV|V|VI|VII|VIII|IX|X|Ⅱ|Ⅲ|Ⅳ"
        )
        internal val chineseSeason: Map<String, Int> = mapOf(
            "一" to 1,
            "二" to 2,
            "三" to 3,
            "四" to 4,
            "五" to 5,
            "六" to 6,
            "七" to 7,
            "八" to 8,
            "九" to 9,
            "十" to 10,
            "II" to 2,
            "III" to 3,
            "IV" to 4,
            "V" to 5,
            "VI" to 6,
            "VII" to 7,
            "VIII" to 8,
            "IX" to 9,
            "X" to 10,
            "Ⅱ" to 2,
            "Ⅲ" to 3,
            "Ⅳ" to 4,
        )
    }

    private data class Matched(val origin: String, val matched: String, val start: Int, val end: Int) {

        fun getNumber(): Int? {
            val d = end.toDouble() / origin.length.toDouble()
            if (d < 0.5) {
                return 1
            }
            val all = this.matched.all { char -> char.isDigit() }
            if (all) {
                return this.matched.toInt()
            }
            return chineseSeason[matched]
        }
    }

}