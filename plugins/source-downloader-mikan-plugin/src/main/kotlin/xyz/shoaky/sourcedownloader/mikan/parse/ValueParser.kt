package xyz.shoaky.sourcedownloader.mikan.parse

internal interface ValueParser {

    val name: String
    fun apply(subjectContent: SubjectContent, filename: String): Result
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

    fun intValue(): Int? {
        return value?.toString()?.toIntOrNull()
    }

    fun padNumber(length: Int = 2): String? {
        val str = value?.toString() ?: return null
        if (str.contains(".")) {
            val index = str.indexOf(".")
            val substring = str.substring(0, index)
            val padded = substring.padStart(length, '0')
            return padded + str.substring(index)
        }
        return padValue(length)
    }

    override fun compareTo(other: Result): Int {
        return accuracy.compareTo(other.accuracy)
    }
}