package io.github.shoaky.sourcedownloader.core.file

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import java.io.InputStream
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path

data class CoreFileContent(
    override val fileDownloadPath: Path,
    val sourceSavePath: Path,
    override val downloadPath: Path,
    override val patternVariables: MapPatternVariables,
    val fileSavePathPattern: CorePathPattern,
    val filenamePattern: CorePathPattern,
    val targetSavePath: Path,
    val targetFilename: String,
    override val attrs: Map<String, Any> = emptyMap(),
    override val tags: Set<String> = emptySet(),
    override val fileUri: URI? = null,
    val errors: List<String> = emptyList(),
    var status: FileContentStatus = FileContentStatus.UNDETECTED,
    @JsonIgnore
    val data: InputStream? = null
) : FileContent {

    private val targetPath: Path = targetSavePath.resolve(targetFilename)

    override fun targetPath(): Path {
        return targetPath
    }

    override fun saveDirectoryPath(): Path {
        return targetSavePath
    }

    fun targetFilename(): String {
        return targetFilename
    }

    override fun fileSaveRootDirectory(): Path? {
        val saveDirectoryPath = saveDirectoryPath()
        if (sourceSavePath == saveDirectoryPath) {
            return null
        }
        val prefix = saveDirectoryPath.toString().removePrefix(sourceSavePath.toString())
        val resolve = sourceSavePath.resolve(Path(prefix).firstOrNull() ?: Path(""))
        return resolve.takeIf { it != sourceSavePath }
    }
}