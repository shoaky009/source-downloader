package io.github.shoaky.sourcedownloader.common.getchu

import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import java.time.LocalDate

data class GetchuDetailItem(
    val getchuId: String? = null,
    val title: String? = null,
    val isbn: String? = null,
    val brand: String? = null,
    val releaseDate: LocalDate? = null
) : PatternVariables {

    override fun variables(): Map<String, String> {
        return mapOf()
    }
}