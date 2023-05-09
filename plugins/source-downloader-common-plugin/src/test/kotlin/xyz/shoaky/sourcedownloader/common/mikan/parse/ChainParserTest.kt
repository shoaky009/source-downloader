package xyz.shoaky.sourcedownloader.common.mikan.parse

import org.junit.jupiter.api.Test

class ChainParserTest {

    @Test
    fun given_false_should_all_parser_executed() {
        val parserChain = ParserChain(listOf(V0, V1), false)
        parserChain.apply(create(""), "")
        assert(V0.executed)
        assert(V1.executed)
    }

    @Test
    fun given_true_should_first_parser_executed() {
        val parserChain = ParserChain(listOf(V0, V1), true)
        parserChain.apply(create(""), "")
        assert(V0.executed)
        assert(!V1.executed)
    }

}

private object V0 : ValueParser {
    override val name: String = "v0"
    var executed = false
    override fun apply(subjectContent: SubjectContent, filename: String): Result {
        executed = true
        return Result(1)
    }
}

private object V1 : ValueParser {
    override val name: String = "v1"
    var executed = false
    override fun apply(subjectContent: SubjectContent, filename: String): Result {
        executed = true
        return Result(1)
    }
}
