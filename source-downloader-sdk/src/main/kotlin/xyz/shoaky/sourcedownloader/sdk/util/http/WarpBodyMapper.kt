package xyz.shoaky.sourcedownloader.sdk.util.http

class WarpBodyMapper<T : Any>(
    private val bodyMapper: BodyMapper<T>
) : BodyMapper<T> {
    override fun mapping(info: MappingInfo<T>): T {
        try {
            return bodyMapper.mapping(info)
        } catch (e: Exception) {
            val body = String(info.bytes, Charsets.UTF_8)
            log.error("mapping error, body: $body", e)
            throw e
        }
    }
}