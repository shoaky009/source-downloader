package io.github.shoaky.sourcedownloader.core.file

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.FileStatus
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import java.io.InputStream
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path

data class CoreFileContent(
    @param:JsonSerialize(using = ToStringSerializer::class)
    override val fileDownloadPath: Path,
    @param:JsonSerialize(using = ToStringSerializer::class)
    val sourceSavePath: Path,
    @param:JsonSerialize(using = ToStringSerializer::class)
    override val downloadPath: Path,
    override val patternVariables: MapPatternVariables,
    val fileSavePathPattern: CorePathPattern,
    val filenamePattern: CorePathPattern,
    @param:JsonSerialize(using = ToStringSerializer::class)
    val targetSavePath: Path,
    val targetFilename: String,
    override val attrs: Map<String, Any> = emptyMap(),
    override val tags: Set<String> = emptySet(),
    override val fileUri: URI? = null,
    val errors: List<String> = emptyList(),
    var status: FileContentStatus = FileContentStatus.UNDETECTED,
    @JsonIgnore
    val data: InputStream? = null,
    /**
     * 只有在process中当[status] == [FileContentStatus.TARGET_EXISTS]时此值不为null
     */
    @param:JsonSerialize(using = ToStringSerializer::class)
    override var existTargetPath: Path? = null,
    // 只为了展示用
    val processedVariables: MapPatternVariables? = null
) : FileContent {

    private val targetPath: Path = targetSavePath.resolve(targetFilename)

    override fun targetPath(): Path {
        return targetPath
    }

    override fun saveDirectoryPath(): Path {
        return targetSavePath
    }

    override fun status(): FileStatus {
        return status
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