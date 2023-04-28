package xyz.shoaky.sourcedownloader.mikan.parse

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.api.bangumi.Subject
import java.nio.file.Files
import java.time.LocalDate
import kotlin.io.path.Path
import kotlin.test.assertEquals

fun create(name: String): SubjectContent {
    val subject = Subject(1, name, name, LocalDate.now(), 12)
    return SubjectContent(subject, name)
}

class SeasonParserChainTest {

    @Test
    fun should_all_expected() {
        val seasonChain = ParserChain.seasonChain()
        Files.readAllLines(Path("src", "test", "resources", "season-test.data"))
            .filter { it.isNullOrBlank().not() }
            .map {
                val split = it.split("|")
                Triple(split[0].toInt(), split[1], split.elementAtOrNull(2) ?: "")
            }
            .forEach {
                val name = it.second
                val season = seasonChain.apply(create(name), it.third).value
                assertEquals(it.first, season, "name:${name}")
            }
    }

}