package io.github.shoaky.sourcedownloader.sdk.util.http

class CommonBodyMapper<T : Any>(
    private val bodyMapper: BodyMapper<T>
) : BodyMapper<T> {

    override fun mapping(info: MappingInfo<T>): T {
        try {
            return bodyMapper.mapping(info)
        } catch (e: Exception) {
            val body = String(info.bytes, Charsets.UTF_8)
            log.warn("Failed to mapping body:{}, message:{}", body, e.message)
            throw e
        }
    }
}