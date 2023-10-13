package io.github.shoaky.sourcedownloader.sdk.util.http

import io.github.shoaky.sourcedownloader.sdk.util.Jackson

interface BodyMapper<T : Any> {

    fun mapping(info: MappingInfo<T>): T
}

class JsonBodyMapper<T : Any> : BodyMapper<T> {

    override fun mapping(info: MappingInfo<T>): T {
        return Jackson.fromJson(String(info.bytes, Charsets.UTF_8), info.type)
    }
}

internal class StringBodyMapper<T : Any> : BodyMapper<T> {

    override fun mapping(info: MappingInfo<T>): T {
        val string = String(info.bytes, Charsets.UTF_8)
        return Jackson.convert(string, info.type)
    }
}

internal class BodyWrapperMapper : BodyMapper<BodyWrapper> {

    override fun mapping(info: MappingInfo<BodyWrapper>): BodyWrapper {
        return BodyWrapper(info.bytes)
    }

}