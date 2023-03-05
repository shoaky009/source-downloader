package xyz.shoaky.sourcedownloader.mikan.parse

internal interface ValueParser {

    val name: String
    fun apply(subjectContent: SubjectContent, filename: String): Result
}

data class Result(
    val value: Any? = null,
    val accuracy: Accuracy = Accuracy.MED
) {
    enum class Accuracy {
        LOW,
        MED,
        ACCURATE
    }

    fun padValue(length: Int = 2): String? {
        return value?.toString()?.padStart(length, '0')
    }

    fun intValue(): Int? {
        return value?.toString()?.toIntOrNull()
    }
}