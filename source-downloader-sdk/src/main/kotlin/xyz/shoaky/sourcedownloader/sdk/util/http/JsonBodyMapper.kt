package xyz.shoaky.sourcedownloader.sdk.util.http

import xyz.shoaky.sourcedownloader.sdk.util.Jackson

internal class JsonBodyMapper<T : Any> : BodyMapper<T> {
    override fun mapping(info: MappingInfo<T>): T {
        return Jackson.fromJson(info.inputStream, info.type)
    }
}