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
 * If [FileContent.saveDirectoryPath] already contains file with same media type and same name without extension, then it will be skipped
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