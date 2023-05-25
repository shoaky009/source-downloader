package xyz.shoaky.sourcedownloader.sdk.util.http

class WarpBodyMapper<T : Any>(
    private val bodyMapper: BodyMapper<T>
) : BodyMapper<T> {
    override fun mapping(info: MappingInfo<T>): T {
        try {
            return bodyMapper.mapping(info)
        } catch (e: Exception) {
            val inputStream = info.inputStream
            if (inputStream.available() == 0) {
                info.inputStream.reset()
            }
            val allBytes = info.inputStream.readAllBytes()
            val body = String(allBytes, Charsets.UTF_8)
            log.error("mapping error, body: $body", e)
            throw e
        }
    }
}