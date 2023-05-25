package xyz.shoaky.sourcedownloader.sdk.util.http

import xyz.shoaky.sourcedownloader.sdk.util.Jackson

internal class StringBodyMapper<T : Any> : BodyMapper<T> {
    override fun mapping(info: MappingInfo<T>): T {
        val string = String(info.bytes, Charsets.UTF_8)
        return Jackson.convert(string, info.type)
    }
}