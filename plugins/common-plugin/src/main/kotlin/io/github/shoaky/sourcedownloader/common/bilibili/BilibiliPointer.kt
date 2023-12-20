package io.github.shoaky.sourcedownloader.common.bilibili

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.SourcePointer
import kotlin.math.max
import kotlin.math.min

data class BilibiliPointer(
    private val favorites: MutableMap<Long, FavoritePointer> = mutableMapOf(),
    // private val uppers: MutableMap<Long, Any> = mutableMapOf()
) : SourcePointer {

    override fun update(itemPointer: ItemPointer) {
        if (itemPointer is MediaItemPointer) {
            favorites.compute(itemPointer.favoriteId) { _, favPointer ->
                if (favPointer == null) {
                    FavoritePointer.fromMedia(itemPointer)
                } else {
                    favPointer.update(itemPointer)
                    favPointer
                }
            }
            return
        }
    }

    fun favButtonTouched(favoriteId: Long): Boolean {
        return favorites[favoriteId]?.touchButton ?: return false
    }

    fun minFavTime(favoriteId: Long): Long? {
        return favorites[favoriteId]?.minFavTime
    }

    fun maxFavTime(favoriteId: Long): Long? {
        return favorites[favoriteId]?.maxFavTime
    }

    fun getFavorites(): Map<Long, FavoritePointer> {
        return favorites
    }
    
}

class FavoritePointer(
    val favoriteId: Long,
    /**
     * B站api收藏时间不正确旧数据都是1594053452，暂时未测是否会有影响
     */
    var minFavTime: Long,
    var maxFavTime: Long,
    var touchButton: Boolean = false
) : ItemPointer {

    fun update(item: MediaItemPointer) {
        if (item.favoriteId != favoriteId) {
            return
        }
        minFavTime = min(minFavTime, item.time)
        maxFavTime = max(maxFavTime, item.time)
        if (touchButton.not()) {
            touchButton = item.touchButton
        }
    }

    companion object {

        fun fromMedia(item: MediaItemPointer): FavoritePointer {
            return FavoritePointer(
                item.favoriteId,
                item.time,
                item.time,
                item.touchButton
            )
        }
    }
}