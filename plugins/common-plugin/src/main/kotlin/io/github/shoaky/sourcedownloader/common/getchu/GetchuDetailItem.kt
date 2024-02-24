package io.github.shoaky.sourcedownloader.common.getchu

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import java.time.LocalDate

data class GetchuDetailItem(
    val getchuId: String? = null,
    val title: String? = null,
    val isbn: String? = null,
    val brand: String? = null,
    @JsonSerialize(using = ToStringSerializer::class)
    val releaseDate: LocalDate? = null
) : PatternVariables