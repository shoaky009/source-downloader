package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.SourcePointer

data class FetchContext<SP : SourcePointer>(
    val pointer: SP,
    val limit: Int,
    val attrs: MutableMap<String, Any> = mutableMapOf(),
) {

    fun <T : Any> loadAttr(key: String, loader: () -> T): T {
        @Suppress("UNCHECKED_CAST")
        return attrs.getOrPut(key) { loader.invoke() } as T
    }
}