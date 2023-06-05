package xyz.shoaky.sourcedownloader.common.anime.parse

import java.nio.file.Path

internal interface ValueParser {

    val name: String
    fun apply(content: SubjectContent, filename: String): Result {
        return apply(content, Path.of(filename))
    }

    fun apply(content: SubjectContent, file: Path): Result
}

data class Result(
    val value: Any? = null,
    val accuracy: Accuracy = Accuracy.MED
) : Comparable<Result> {
    enum class Accuracy {
        LOW,
        MED,
        ACCURATE
    }

    fun padValue(length: Int = 2): String? {
        return value?.toString()?.padStart(length, '0')
    }

    override fun compareTo(other: Result): Int {
        return accuracy.compareTo(other.accuracy)
    }
}