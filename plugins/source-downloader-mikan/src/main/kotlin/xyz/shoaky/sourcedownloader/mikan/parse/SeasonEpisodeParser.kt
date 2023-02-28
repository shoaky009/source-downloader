package xyz.shoaky.sourcedownloader.mikan.parse

internal interface SeasonEpisodeParser {

    val name: String
    fun apply(subjectContent: SubjectContent, filename: String): Result
}

data class Result(val season: Int?,
                  val episode: Int?,
                  val priority: Priority = Priority.NORMAL) {
    enum class Priority {
        LOW,
        NORMAL,
        HIGH
    }
}