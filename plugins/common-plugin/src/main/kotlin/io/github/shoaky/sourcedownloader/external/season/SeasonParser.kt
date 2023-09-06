package io.github.shoaky.sourcedownloader.external.season

interface SeasonParser {

    fun input(subject: String): SeasonResult?

}