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
        val files = mutableListOf<SourceFile>()

        val fanboxImages = post.body.images.toMutableList()
        fanboxImages.addAll(post.body.imageMap.values)
        val images = fanboxImages.distinct().mapIndexed { _, image ->
            SourceFile(
                Path("${image.id}.${image.extension}"),
                mapOf(
                    "height" to image.height,
                    "width" to image.width,
                ),
                image.originalUrl
            )
        }
        files.addAll(images)

        val fanboxFiles = post.body.files.toMutableList()
        fanboxFiles.addAll(post.body.fileMap.values)

        files.addAll(
            fanboxFiles.distinct().mapIndexed { _, file ->
                SourceFile(
                    Path("${file.id}.${file.extension}"),
                    mapOf(
                        "size" to file.size,
                    ),
                    file.url
                )
            }
        )

        // 如果需要网页文本内容，应用启动时开启一个Internal的HttpServer把网页内容返回，待定暂时不实现
        // val create = HttpServer.create()
        // val createContext = create.createContext("/fanbox/${sourceItem.link.path}")
        // createContext.setHandler {
        //
        // }
        return files
    }
}