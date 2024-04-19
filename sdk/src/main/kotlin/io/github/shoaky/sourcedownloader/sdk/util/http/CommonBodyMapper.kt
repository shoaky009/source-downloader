package io.github.shoaky.sourcedownloader.sdk.util.http

class CommonBodyMapper<T : Any>(
    private val bodyMapper: BodyMapper<T>
) : BodyMapper<T> {

    override fun mapping(info: MappingInfo<T>): T {
        return bodyMapper.mapping(info)
    }
}