package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.external.fanbox.PostInfoRequest
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import kotlin.io.path.Path

class FanboxFileResolver(
    private val fanboxClient: FanboxClient
) : ItemFileResolver {

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val request = PostInfoRequest(sourceItem.link.path.split("/").last())
        val post = fanboxClient.execute(request).body().body

        return post.body.images.mapIndexed { _, image ->
            SourceFile(
                Path(image.id),
                mapOf(
                    "type" to "image",
                    "height" to image.height,
                    "width" to image.width,
                ),
                image.originalUrl
            )
        }
    }
}