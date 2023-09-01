package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileExistsDetector
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import org.apache.tika.Tika
import org.apache.tika.mime.MediaType
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

/**
 * 如果 [FileContent.saveDirectoryPath] 已经包含了相同媒体类型并且文件名(不包含扩展名)一样的文件，那么就认为已经存在
 */
object MediaTypeExistsDetector : FileExistsDetector {

    /**
     * 并非所有类型都支持，后续需要继续添砖加瓦
     */
    private val tika = Tika()

    override fun exists(fileMover: FileMover, content: ItemContent): Map<Path, Boolean> {
        val savePaths = content.sourceFiles.groupBy { it.saveDirectoryPath() }

        val result = mutableMapOf<Path, Boolean>()
        for (entry in savePaths) {
            val path = entry.key
            val currentPathsMediaMapping = fileMover.listPath(path)
                .groupBy({
                    MediaType.parse(tika.detect(it.name)).type
                }, {
                    it.nameWithoutExtension
                })
                .mapValues { it.value.toSet() }
            entry.value.map { it.targetPath() }.forEach { target ->
                val mediaType = MediaType.parse(tika.detect(target.name)).type
                val currentPath = currentPathsMediaMapping[mediaType] ?: emptySet()
                result[target] = currentPath.contains(target.nameWithoutExtension)
            }
        }
        return result
    }
}