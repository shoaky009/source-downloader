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
    fun attributes(): Map<String, List<String>> {
        return sourceFiles.map { it.patternVars.getVars().entries }
            .flatten()
            .groupBy({ it.key }, { it.value })
    }

    fun canRenameFiles(): List<SourceFileContent> {
        return sourceFiles
            .filter { it.targetFilePath().notExists() }
            .filter { it.fileDownloadPath.exists() }
    }

}

data class SourceFileContent(
    val fileDownloadPath: Path,
    val sourceSavePath: Path,
    val patternVars: PatternVars,
    val fileSavePathPattern: PathPattern,
    val filenamePattern: PathPattern
) {

    fun targetFilePath(): Path {
        return saveDirectoryPath().resolve(targetFilename())
    }

    fun saveDirectoryPath(): Path {
        return sourceSavePath.resolve(fileSavePathPattern.resolve(patternVars))
    }

    fun targetFilename(): String {
        val targetFilename = filenamePattern.resolve(patternVars)
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

    fun resolve(vars: PatternVars): String {
        val matcher = PATTERN.matcher(pattern)
        val result = StringBuilder()
        val variables = vars.getVars()
        while (matcher.find()) {
            val variableName = matcher.group(1)
            val variableValue = variables.getOrDefault(variableName, "")
            matcher.appendReplacement(result, variableValue)
        }
        matcher.appendTail(result)
        return result.toString()
    }

    companion object {
        var PATTERN: Pattern = Pattern.compile("\\{(.+?)}")
        val ORIGIN = PathPattern("")
    }
}