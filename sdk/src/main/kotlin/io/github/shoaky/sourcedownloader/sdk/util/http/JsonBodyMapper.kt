package io.github.shoaky.sourcedownloader.sdk.util.http

import io.github.shoaky.sourcedownloader.sdk.util.Jackson

class JsonBodyMapper<T : Any> : BodyMapper<T> {

    override fun mapping(info: MappingInfo<T>): T {
        return Jackson.fromJson(String(info.bytes, Charsets.UTF_8), info.type)
    }
}