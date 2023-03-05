package xyz.shoaky.sourcedownloader.mikan.parse

import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.assertEquals

class EpisodeParserChainTest {
    @Test
    fun should_all_expected() {
        val episodeChain = ParserChain.episodeChain()
        Files.readAllLines(Path("src/test/resources/episode-test.data"))
            .filter { it.isNullOrBlank().not() }
            .map {
                val split = it.split("|")
                Triple(split[0].toInt(), split[1], split[1])
            }
            .forEach {
                val name = it.second
                val res = episodeChain.apply(create(name), it.third).intValue()
                assertEquals(it.first, res, "name:${name}")
            }
    }
}