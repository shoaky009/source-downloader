package xyz.shoaky.sourcedownloader.sdk.util.http

import com.fasterxml.jackson.core.type.TypeReference

data class MappingInfo<T : Any>(
    val type: TypeReference<T>,
    val subtype: String,
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MappingInfo<*>

        if (type != other.type) return false
        if (subtype != other.subtype) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + subtype.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}