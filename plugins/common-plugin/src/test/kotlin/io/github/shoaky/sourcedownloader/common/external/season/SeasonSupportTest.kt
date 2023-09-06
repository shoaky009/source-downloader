package io.github.shoaky.sourcedownloader.common.external.season

import io.github.shoaky.sourcedownloader.external.season.ParseValue
import io.github.shoaky.sourcedownloader.external.season.SeasonParser
import io.github.shoaky.sourcedownloader.external.season.SeasonResult
import io.github.shoaky.sourcedownloader.external.season.SeasonSupport
import org.junit.jupiter.api.Test

class SeasonSupportTest {

    @Test
    fun given_false_should_all_parser_executed() {
        val v0 = TestSeasonParser(1)
        val v1 = TestSeasonParser(1)
        val support = SeasonSupport(listOf(v0, v1))
        support.input(ParseValue("test", anyResult = false))
        assert(v0.executed)
        assert(v1.executed)
    }

    @Test
    fun given_true_should_first_parser_executed() {
        val v0 = TestSeasonParser(1)
        val v1 = TestSeasonParser(1)
        val support = SeasonSupport(listOf(v0, v1))
        support.input(ParseValue("test"))
        assert(v0.executed)
        assert(!v1.executed)
    }

}

private class TestSeasonParser(
    val value: Int
) : SeasonParser {

    var executed = false

    override fun input(subject: String): SeasonResult? {
        executed = true
        return SeasonResult(value)
    }
}