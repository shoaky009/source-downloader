package io.github.shoaky.sourcedownloader.external.season

data class SeasonResult(
    val value: Int? = null,
    val accuracy: Accuracy = Accuracy.MED
) : Comparable<SeasonResult> {

    override fun compareTo(other: SeasonResult): Int {
        return accuracy.compareTo(other.accuracy)
    }

    enum class Accuracy {
        LOW,
        MED,
        ACCURATE
    }
}