package xyz.shoaky.sourcedownloader.core.file

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import xyz.shoaky.sourcedownloader.core.CorePathPattern
import xyz.shoaky.sourcedownloader.sdk.FileContent
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.PatternVariables
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name

data class CoreFileContent(
    override val fileDownloadPath: Path,
    val sourceSavePath: Path,
    override val downloadPath: Path,
    override val patternVariables: MapPatternVariables,
    @JsonDeserialize(`as` = CorePathPattern::class)
    val fileSavePathPattern: PathPattern,
    @JsonDeserialize(`as` = CorePathPattern::class)
    val filenamePattern: PathPattern,
    override val attributes: Map<String, Any> = emptyMap(),
    var status: FileContentStatus = FileContentStatus.NORMAL
) : FileContent {

    private var variableErrorStrategy: VariableErrorStrategy = VariableErrorStrategy.STAY

    @Transient
    private val allVariables = MapPatternVariables(patternVariables)

    @Transient
    override val tags: MutableSet<String> = mutableSetOf()

    private val targetPath: Path by lazy {
        saveDirectoryPath().resolve(targetFilename())
    }

    fun setVariableErrorStrategy(strategy: VariableErrorStrategy) {
        variableErrorStrategy = strategy
    }

    override fun targetPath(): Path {
        return targetPath
    }

    override fun saveDirectoryPath(): Path {
        val parse = fileSavePathPattern.parse(allVariables)
        if (parse.success()) {
            return sourceSavePath.resolve(parse.path)
        }

        return when (variableErrorStrategy) {
            VariableErrorStrategy.ORIGINAL,
            VariableErrorStrategy.STAY -> {
                fileDownloadPath.parent
            }

            VariableErrorStrategy.PATTERN -> {
                sourceSavePath.resolve(parse.path)
            }
        }
    }

    fun addSharedVariables(shared: PatternVariables) {
        allVariables.addVariables(shared)
    }

    fun targetFilename(): String {
        if (filenamePattern == CorePathPattern.ORIGIN) {
            return fileDownloadPath.name
        }
        val parse = filenamePattern.parse(allVariables)
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

        return when (variableErrorStrategy) {
            VariableErrorStrategy.STAY,
            VariableErrorStrategy.ORIGINAL -> {
                fileDownloadPath.name
            }

            VariableErrorStrategy.PATTERN -> {
                val target = parse.path
                val extension = fileDownloadPath.extension
                if (target.endsWith(extension)) {
                    return target
                }
                "$target.$extension"
            }
        }
    }

    override fun itemSaveRootDirectory(): Path? {
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

    override fun itemDownloadRootDirectory(): Path? {
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

    fun tags(): List<String> {
        return tags.toList()
    }

    fun currentVariables(): PatternVariables {
        return allVariables
    }
}