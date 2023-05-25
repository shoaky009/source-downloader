package xyz.shoaky.sourcedownloader.sdk.util.http

import com.fasterxml.jackson.core.type.TypeReference
import java.io.InputStream

data class MappingInfo<T : Any>(
    val type: TypeReference<T>,
    val subtype: String,
    val inputStream: InputStream
)