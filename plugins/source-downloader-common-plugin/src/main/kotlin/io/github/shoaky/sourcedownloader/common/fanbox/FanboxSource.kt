package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.sdk.NullPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.component.Source

class FanboxSource(
    val sessionId: String,
) : Source<NullPointer> {
    override fun fetch(pointer: NullPointer?, limit: Int): Iterable<PointedItem<NullPointer>> {
        TODO("Not yet implemented")
    }

}

