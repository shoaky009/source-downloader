package io.github.shoaky.sourcedownloader.common.anime.parse

import java.nio.file.Path

object DefaultValueSeasonParser : ValueParser {

    override val name: String = "default"

    override fun apply(content: SubjectContent, file: Path): Result {
        return Result(1, Result.Accuracy.LOW)
    }
}