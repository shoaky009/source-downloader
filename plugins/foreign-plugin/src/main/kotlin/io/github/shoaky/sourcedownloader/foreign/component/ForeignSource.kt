package io.github.shoaky.sourcedownloader.foreign.component

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.foreign.ForeignIterator
import io.github.shoaky.sourcedownloader.foreign.ForeignPointer
import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.foreign.methods.SourceForeignMethods
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.component.Source

class ForeignSource(
    private val client: ForeignStateClient,
    private val methods: SourceForeignMethods,
) : Source<ForeignPointer> {

    override fun fetch(pointer: ForeignPointer, limit: Int): Iterable<PointedItem<ItemPointer>> {
        return client.postState(
            methods.fetch,
            mapOf("pointer" to pointer, "limit" to limit),
            jacksonTypeRef<ForeignIterator>()
        ).also {
            it.foreignStateClient = this.client
            it.nextPath = methods.next
            it.hasNextPath = methods.hasNext
        }
    }

    override fun defaultPointer(): ForeignPointer {
        val pointer = ForeignPointer()
        pointer.foreignStateClient = this.client
        pointer.pointerUpdatePath = methods.pointerUpdate
        return pointer
    }

}

