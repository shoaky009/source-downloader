package io.github.shoaky.sourcedownloader.foreign

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem

class ForeignIterator : Iterator<PointedItem<ItemPointer>>, Iterable<PointedItem<ItemPointer>> {

    @JsonAnySetter
    @JsonAnyGetter
    val state: MutableMap<String, Any> = mutableMapOf()

    @JsonIgnore
    lateinit var foreignStateClient: ForeignStateClient

    @JsonIgnore
    lateinit var nextPath: String

    @JsonIgnore
    lateinit var hasNextPath: String

    override fun iterator(): Iterator<PointedItem<ItemPointer>> {
        return this
    }

    override fun hasNext(): Boolean {
        val postState = foreignStateClient.postState(hasNextPath, state, jacksonTypeRef<JsonNode>())
        return postState.get("result").booleanValue()
    }

    override fun next(): PointedItem<ItemPointer> {
        val postState = foreignStateClient.postState(nextPath, state, jacksonTypeRef<PointedItem<ItemPointer>>())
        return postState
    }


}