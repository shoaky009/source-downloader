package io.github.shoaky.sourcedownloader.foreign

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.SourcePointer

class ForeignPointer(
    val data: MutableMap<String, Any> = mutableMapOf()
) : SourcePointer {

    @JsonIgnore
    lateinit var foreignStateClient: ForeignStateClient

    @JsonIgnore
    lateinit var pointerUpdatePath: String

    override fun update(itemPointer: ItemPointer) {
        TODO()
    }

}