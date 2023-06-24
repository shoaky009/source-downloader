package io.github.shoaky.sourcedownloader.core.file

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.shoaky.sourcedownloader.core.CorePathPattern
import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PathPattern
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
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
    var status: FileContentStatus = FileContentStatus.NORMAL,
    override val tags: MutableSet<String> = mutableSetOf()
) : FileContent {

    private var variableErrorStrategy: VariableErrorStrategy = VariableErrorStrategy.STAY

    @Transient
    private val allVariables = MapPatternVariables(patternVariables)

    private val targetPath: Path by lazy {
        val saveDirectoryPath = saveDirectoryPath()
        val result = targetFilename0()
        if (result.second.not() && variableErrorStrategy == VariableErrorStrategy.STAY) {
            return@lazy fileDownloadPath
        }
        saveDirectoryPath.resolve(result.first)
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

    private fun targetFilename0(): Pair<String, Boolean> {
        if (filenamePattern == CorePathPattern.ORIGIN) {
            return fileDownloadPath.name to true
        }
        val parse = filenamePattern.parse(allVariables)
        val success = parse.success()
        if (success) {
            val targetFilename = parse.path
            if (targetFilename.isBlank()) {
                return fileDownloadPath.name to true
            }

            val extension = fileDownloadPath.extension
            if (targetFilename.endsWith(extension)) {
                return targetFilename to true
            }
            return "$targetFilename.$extension" to true
        }

        return when (variableErrorStrategy) {
            VariableErrorStrategy.STAY,
            VariableErrorStrategy.ORIGINAL -> {
                fileDownloadPath.name to false
            }

            VariableErrorStrategy.PATTERN -> {
                val target = parse.path
                val extension = fileDownloadPath.extension
                if (target.endsWith(extension)) {
                    return target to false
                }
                "$target.$extension" to false
            }
        }
    }

    fun targetFilename(): String {
        return targetFilename0().first
    }

    override fun fileSaveRootDirectory(): Path? {
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

    fun isTagged(tag: String): Boolean {
        return tags.contains(tag)
    }

    fun currentVariables(): PatternVariables {
        return allVariables
    }
}