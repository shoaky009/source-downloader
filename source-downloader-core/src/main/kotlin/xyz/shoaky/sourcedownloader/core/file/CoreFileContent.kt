package xyz.shoaky.sourcedownloader.core.file

import xyz.shoaky.sourcedownloader.sdk.FileContent
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.PatternVariables
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.extension
import kotlin.io.path.name

data class CoreFileContent(
    override val fileDownloadPath: Path,
    val sourceSavePath: Path,
    override val downloadPath: Path,
    override val patternVariables: MapPatternVariables,
    val fileSavePathPattern: PathPattern,
    val filenamePattern: PathPattern,
) : FileContent {

    @Transient
    private val sharedVariables = SharedPatternVariables(patternVariables)

    @Transient
    private val tags: MutableSet<String> = mutableSetOf()

    private val targetPath: Path by lazy {
        saveDirectoryPath().resolve(targetFilename())
    }

    override fun targetPath(): Path {
        return targetPath
    }

    override fun saveDirectoryPath(): Path {
        val parse = fileSavePathPattern.parse(sharedVariables)
        return sourceSavePath.resolve(parse.path)
    }

    fun addSharedVariables(shared: PatternVariables) {
        sharedVariables.addShared(shared)
    }

    fun targetFilename(): String {
        if (filenamePattern == PathPattern.ORIGIN) {
            return fileDownloadPath.name
        }
        val parse = filenamePattern.parse(sharedVariables)
        val success = parse.success()
        if (success) {
            val targetFilename = parse.path
            if (targetFilename.isBlank()) {
                return fileDownloadPath.name
            }

            val extension = fileDownloadPath.extension
            if (targetFilename.endsWith(extension)) {
                return targetFilename
            }
            return "$targetFilename.$extension"
        }

        return fileDownloadPath.name
    }

    /**
     * 获取item文件对应的顶级目录e.g. 文件保存在下/mnt/bangumi/FATE/Season 01 返回 /mnt/bangumi/FATE
     * @return null如果item的文件是保存在saveRootPath下
     */
    override fun saveItemFileRootDirectory(): Path? {
        val saveDirectoryPath = saveDirectoryPath()
        if (sourceSavePath == saveDirectoryPath) {
            return null
        }
        val depth = fileSavePathPattern.depth()
        var res = saveDirectoryPath
        for (i in 0..depth) {
            res = saveDirectoryPath.parent
        }
        if (sourceSavePath == res) {
            return null
        }
        return res
    }

    override fun downloadItemFileRootDirectory(): Path? {
        if (fileDownloadPath.parent == downloadPath) {
            return null
        }

        var path = fileDownloadPath.parent
        while (path.parent != downloadPath) {
            path = path.parent
        }
        return path
    }

    fun tag(tags: List<String>) {
        this.tags.addAll(tags)
    }

    fun isTagged(tag: String): Boolean {
        return tags.contains(tag)
    }

    companion object {

        private val defaultPermissions = setOf(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.OTHERS_READ,
            PosixFilePermission.OTHERS_EXECUTE
        )
        private val fileAttribute = PosixFilePermissions.asFileAttribute(defaultPermissions)
    }
}