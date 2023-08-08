package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import io.github.shoaky.sourcedownloader.sdk.component.ItemExistsDetector
import org.apache.tika.Tika
import org.apache.tika.mime.MediaType
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension


/**
 * 如果 [FileContent.saveDirectoryPath] 已经包含了相同媒体类型并且文件名(不包含扩展名)一样的文件，那么就认为已经存在
 */
object MediaTypeExistsDetector : ItemExistsDetector {

    /**
     * 并非所有类型都支持，后续需要继续添砖加瓦
     */
    private val tika = Tika()

    override fun exists(fileMover: FileMover, content: ItemContent): Boolean {
        val savePaths = content.sourceFiles.groupBy { it.saveDirectoryPath() }

        for (entry in savePaths) {
            val currentPathsMediaMapping = fileMover.listPath(entry.key)
                .groupBy({
                    MediaType.parse(tika.detect(it.name)).type
                }, {
                    it.nameWithoutExtension
                })
                .mapValues { it.value.toSet() }
            val anyNotExists = entry.value.map { it.targetPath() }.any { target ->
                val mediaType = MediaType.parse(tika.detect(target.name)).type
                val currentPath = currentPathsMediaMapping[mediaType] ?: emptySet()
                currentPath.contains(target.nameWithoutExtension).not()
            }
            if (anyNotExists) {
                return false
            }
        }
        return true
    }
}