package xyz.shoaky.sourcedownloader.common.fanbox

import xyz.shoaky.sourcedownloader.sdk.NullPointer
import xyz.shoaky.sourcedownloader.sdk.PointedItem
import xyz.shoaky.sourcedownloader.sdk.component.Source

class FanboxSource(
    val sessionId: String,
) : Source<NullPointer> {
    override fun fetch(pointer: NullPointer?, limit: Int): Iterable<PointedItem<NullPointer>> {
        TODO("Not yet implemented")
    }

}

