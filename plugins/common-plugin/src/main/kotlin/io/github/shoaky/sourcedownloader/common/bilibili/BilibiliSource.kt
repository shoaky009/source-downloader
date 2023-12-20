package io.github.shoaky.sourcedownloader.common.bilibili

import io.github.shoaky.sourcedownloader.external.bilibili.BilibiliClient
import io.github.shoaky.sourcedownloader.external.bilibili.GetFavorites
import io.github.shoaky.sourcedownloader.external.bilibili.Media
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Source
import io.github.shoaky.sourcedownloader.sdk.util.ExpandIterator
import io.github.shoaky.sourcedownloader.sdk.util.RequestResult
import java.time.Instant
import java.time.ZoneId

/**
 * B站收藏夹可能会有BUG，当收藏时间在1594053452之前的视频超过每次fetch的条数可能无法触底
 */
class BilibiliSource(
    private val client: BilibiliClient = BilibiliClient(),
    private val favorites: List<Long> = emptyList(),
    // private val uppers: List<Long> = emptyList()
) : Source<BilibiliPointer> {

    override fun fetch(pointer: BilibiliPointer, limit: Int): Iterable<PointedItem<ItemPointer>> {
        val favPage: MutableMap<Long, Int> = mutableMapOf()
        return ExpandIterator(favorites, limit) { favoriteId ->
            // 先偷懒简单实现，一次性获取完
            val page = favPage.compute(favoriteId) { _, page ->
                page?.plus(1) ?: 1
            } ?: 1

            val data = client.execute(GetFavorites(favoriteId, page)).body().data
            val buttonTouched = pointer.favButtonTouched(favoriteId)
            val medias = if (buttonTouched) {
                val maxFavTime = pointer.maxFavTime(favoriteId) ?: Long.MIN_VALUE
                data.medias.filter { it.favTime > maxFavTime }
            } else {
                val minFavTime = pointer.minFavTime(favoriteId) ?: Long.MAX_VALUE
                data.medias.filter { it.favTime <= minFavTime }
            }
            val last = medias.lastOrNull()
            // attr == 0为有效的视频
            val media = medias.filter { it.attr == 0 }.map {
                fromMedia(favoriteId, it, data.hasMore.not() && last == it)
            }
            RequestResult(media, data.hasMore.not() || (buttonTouched && media.isEmpty()))
        }.asIterable()
    }

    override fun defaultPointer(): BilibiliPointer {
        return BilibiliPointer()
    }

    companion object {

        fun fromMedia(favoriteId: Long, media: Media, oldestFavItem: Boolean): PointedItem<ItemPointer> {
            val item = SourceItem(
                media.title,
                media.link,
                Instant.ofEpochSecond(media.pubtime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime(),
                "video",
                media.link,
                // 后面看需求加，目前只需要这些
                mapOf(
                    "upper" to media.upper.name,
                    "type" to media.type,
                    "page" to media.page,
                    "bv" to media.bvId,
                )
            )

            return PointedItem(
                item, MediaItemPointer(favoriteId, media.favTime, oldestFavItem)
            )
        }
    }
}