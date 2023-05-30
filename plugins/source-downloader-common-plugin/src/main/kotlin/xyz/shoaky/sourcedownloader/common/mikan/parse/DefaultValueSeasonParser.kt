package xyz.shoaky.sourcedownloader.common.mikan.parse

import java.nio.file.Path

object DefaultValueSeasonParser : ValueParser {

    override val name: String = "default"

    override fun apply(content: String, file: Path): Result {
        return Result(1, Result.Accuracy.LOW)
    }
}