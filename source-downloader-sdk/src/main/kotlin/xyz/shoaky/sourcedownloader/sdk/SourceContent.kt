package xyz.shoaky.sourcedownloader.sdk

import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.regex.Pattern
import kotlin.io.path.*

data class SourceContent(
    val sourceItem: SourceItem,
    val sourceFiles: List<SourceFileContent>,
) {

    fun canRenameFiles(): List<SourceFileContent> {
        return sourceFiles
            .filter { it.targetPath().notExists() }
            .filter { it.fileDownloadPath.exists() }
    }

    fun allTargetPaths(): List<Path> {
        return sourceFiles.map { it.targetPath() }
    }

    fun summarySubject(): String {
        if (sourceFiles.size == 1) {
            return sourceFiles.first().targetPath().name
        }
        return "${sourceItem.title}内的${sourceFiles.size}个文件"
    }
}

data class SourceFileContent(
    val fileDownloadPath: Path,
    val sourceSavePath: Path,
    val patternVariables: MapPatternVariables,
    val fileSavePathPattern: PathPattern,
    val filenamePattern: PathPattern
) {

    private val targetPath: Path by lazy {
        saveDirectoryPath().resolve(targetFilename())
    }

    fun targetPath(): Path {
        return targetPath
    }

    fun saveDirectoryPath(): Path {
        return sourceSavePath.resolve(fileSavePathPattern.resolve(patternVariables))
    }

    fun targetFilename(): String {
        val targetFilename = filenamePattern.resolve(patternVariables)
        if (targetFilename.isBlank()) {
            return fileDownloadPath.name
        }
        val extension = fileDownloadPath.extension
        if (targetFilename.endsWith(extension)) {
            return targetFilename
        }
        return "$targetFilename.$extension"
    }

    fun createSaveDirectories() {
        val targetSaveDirectoryPath = saveDirectoryPath()
        if (targetSaveDirectoryPath.notExists()) {
            targetSaveDirectoryPath.createDirectories(fileAttribute)
        }
    }

    /**
     * 获取item文件对应的顶级目录e.g. 文件保存在下/mnt/bangumi/FATE/Season 01 返回 /mnt/bangumi/FATE
     * @return null如果item的文件是保存在saveRootPath下
     */
    fun itemFileRootDirectory(): Path? {
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

    companion object {

        private val permissions = setOf(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.OTHERS_READ,
            PosixFilePermission.OTHERS_EXECUTE
        )
        private val fileAttribute = PosixFilePermissions.asFileAttribute(permissions)
    }

}

data class PathPattern(val pattern: String) {

    fun resolve(provider: PatternVariables): String {
        val matcher = VARIABLE_PATTERN.matcher(pattern)
        val result = StringBuilder()
        val variables = provider.getVariables()
        while (matcher.find()) {
            val variableName = matcher.group(1)
            val variableValue = variables.getOrDefault(variableName, "")
            matcher.appendReplacement(result, variableValue)
        }
        matcher.appendTail(result)
        return result.toString()
    }

    fun depth(): Int {
        return pattern.split("/").size
    }

    companion object {
        var VARIABLE_PATTERN: Pattern = Pattern.compile("\\{(.+?)}")
        val ORIGIN = PathPattern("")
    }
}