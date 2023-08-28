package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.external.fanbox.PostInfoRequest
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import kotlin.io.path.Path

/**
 * Fanbox的文件解析器，通过postId调用fanbox api获取文件信息
 */
class FanboxFileResolver(
    private val fanboxClient: FanboxClient
) : ItemFileResolver {

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val request = PostInfoRequest(sourceItem.link.path.split("/").last())
        val post = fanboxClient.execute(request).body().body
        val media = post.body
        val sourceFiles = mutableListOf<SourceFile>()
        post.coverImageUrl?.apply {
            sourceFiles.add(
                SourceFile(
                    Path("cover_${post.id}.jpeg"),
                    mapOf("type" to "cover"),
                    this
                )
            )
        }

        val images = media.imagesOrdering().mapIndexed { _, image ->
            SourceFile(
                Path("${image.id}.${image.extension}"),
                mapOf(
                    "height" to image.height,
                    "width" to image.width,
                    "type" to "image"
                ),
                image.originalUrl
            )
        }
        sourceFiles.addAll(images)

        val fanboxFiles = media.filesOrdering().mapIndexed { _, file ->
            SourceFile(
                Path("${file.name}.${file.extension}"),
                mapOf(
                    "size" to file.size,
                    "type" to "file"
                ),
                file.url
            )
        }
        sourceFiles.addAll(fanboxFiles)

        val textBlock = media.joinTextBlock()
        if (textBlock.trim().isNotBlank()) {
            sourceFiles.add(
                SourceFile(
                    Path("text.txt"),
                    mapOf(
                        "type" to "text",
                    ),
                    data = textBlock.byteInputStream()
                )
            )
        }

        val htmls = media.urlEmbedsOrdering().mapNotNull { url ->
            url.html?.let {
                SourceFile(
                    Path("${url.id}.html"),
                    mapOf(
                        "type" to "html",
                    ),
                    data = it.byteInputStream()
                )
            }
        }
        sourceFiles.addAll(htmls)
        return sourceFiles
    }
}