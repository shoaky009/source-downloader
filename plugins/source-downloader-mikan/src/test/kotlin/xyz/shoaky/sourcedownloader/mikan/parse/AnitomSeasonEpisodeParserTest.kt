package xyz.shoaky.sourcedownloader.mikan.parse

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AnitomSeasonEpisodeParserTest {
    private val parser = AnitomSeasonEpisodeParser()

    @Test
    fun normal() {
        val apply = parser.apply(create("手工少女!!"), "[SweetSub&LoliHouse] 手工少女!! / Do It Yourself!! - 07v2 [WebRip 1080p HEVC-10bit AAC][简繁日内封字幕]")
        assertEquals(7, apply.episode)
    }

}