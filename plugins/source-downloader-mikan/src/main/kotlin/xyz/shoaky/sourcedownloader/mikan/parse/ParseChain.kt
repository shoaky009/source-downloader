package xyz.shoaky.sourcedownloader.mikan.parse

import org.slf4j.LoggerFactory
import xyz.shoaky.sourcedownloader.sdk.api.bangumi.Subject

class ParseChain {

    private val results = mutableMapOf<String, Result>()

    fun apply(mainName: SubjectContent, filename: String): Result {
        for (parse in defaultChain) {
            parse.runCatching {
                results[parse.name] = parse.apply(mainName, filename)
            }.onFailure {
                log.error("${parse.name} 发生异常,name:$mainName filename:$filename {}", it)
            }
        }
        //暂时先这样后面根据情况调整
        val season = results.values.mapNotNull { it.season }.groupingBy { it }.eachCount().maxByOrNull { it.value }
        val episode = results.values.mapNotNull { it.episode }.groupingBy { it }.eachCount().maxByOrNull { it.value }
        return Result(season?.key, episode?.key)
    }

    companion object {
        private val defaultChain: List<SeasonEpisodeParser> = listOf(
            AnitomSeasonEpisodeParser(),
            ParseSeasonFromOriginName()
        )
        private val log = LoggerFactory.getLogger(ParseChain::class.java)
    }
}

data class SubjectContent(val subject: Subject, val mikanTitle: String)