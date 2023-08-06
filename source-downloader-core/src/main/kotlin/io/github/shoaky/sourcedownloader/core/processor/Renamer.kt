package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.CorePathPattern
import io.github.shoaky.sourcedownloader.core.VariableReplacer
import io.github.shoaky.sourcedownloader.core.file.CoreFileContent
import io.github.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PathPattern
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class Renamer(
    private val variableErrorStrategy: VariableErrorStrategy = VariableErrorStrategy.STAY,
    private val variableReplacers: List<VariableReplacer> = emptyList(),
) {

    fun createFileContent(
        sourceItem: SourceItem,
        rawFile: RawFileContent,
        group: PatternVariables,
    ): CoreFileContent {
        val ctx = ProcessingContext(sourceItem, rawFile, group)
        val dirResult = saveDirectoryPath(ctx)
        val filenameResult = targetFilename(ctx)
        val errors = mutableListOf<String>()
        errors.addAll(dirResult.result.failedExpression())
        errors.addAll(filenameResult.result.failedExpression())

        if (filenameResult.result.success().not() && variableErrorStrategy == VariableErrorStrategy.STAY) {
            val fileDownloadPath = rawFile.fileDownloadPath
            return rawFile.createContent(fileDownloadPath.parent, fileDownloadPath.name, errors)
        }
        return rawFile.createContent(dirResult.value, filenameResult.value, errors)
    }

    private fun targetFilename(ctx: ProcessingContext): ResultWrapper<String> {
        val fileDownloadPath = ctx.file.fileDownloadPath
        val filenamePattern = ctx.file.filenamePattern
        if (filenamePattern == CorePathPattern.ORIGIN) {
            return ResultWrapper.fromFilename(fileDownloadPath.name)
        }
        val parse = parse(ctx.patternVariables, filenamePattern, ctx.extraVariables)
        val success = parse.success()
        if (success) {
            val targetFilename = parse.path
            if (targetFilename.isBlank()) {
                return ResultWrapper.fromFilename(fileDownloadPath.name, parse)
            }
            val extension = fileDownloadPath.extension
            if (targetFilename.endsWith(extension)) {
                return ResultWrapper.fromFilename(targetFilename, parse)
            }
            return ResultWrapper.fromFilename("$targetFilename.$extension", parse)
        }

        return when (variableErrorStrategy) {
            VariableErrorStrategy.STAY,
            VariableErrorStrategy.TO_UNRESOLVED,
            VariableErrorStrategy.ORIGINAL -> {
                ResultWrapper.fromFilename(fileDownloadPath.name, parse)
            }

            VariableErrorStrategy.PATTERN -> {
                val target = parse.path
                val extension = fileDownloadPath.extension
                if (target.endsWith(extension)) {
                    return ResultWrapper.fromFilename(target, parse)
                }

                ResultWrapper.fromFilename("$target.$extension", parse)
            }
        }
    }

    private fun saveDirectoryPath(ctx: ProcessingContext): ResultWrapper<Path> {
        val file = ctx.file
        val fileSavePathPattern = file.savePathPattern
        val sourceSavePath = file.sourceSavePath
        val fileDownloadPath = file.fileDownloadPath
        val parse = parse(ctx.patternVariables, fileSavePathPattern, ctx.extraVariables)
        if (parse.success()) {
            if (VariableErrorStrategy.TO_UNRESOLVED == variableErrorStrategy) {
                val success = parse(ctx.patternVariables, file.filenamePattern, ctx.extraVariables).success()
                if (success.not()) {
                    return ResultWrapper(sourceSavePath.resolve(parse.path).resolve(UNRESOLVED), parse)
                }
            }
            return ResultWrapper(sourceSavePath.resolve(parse.path), parse)
        }

        return when (variableErrorStrategy) {
            VariableErrorStrategy.ORIGINAL,
            VariableErrorStrategy.STAY -> {
                ResultWrapper(fileDownloadPath.parent, parse)
            }

            VariableErrorStrategy.PATTERN -> {
                ResultWrapper(sourceSavePath.resolve(parse.path), parse)
            }

            VariableErrorStrategy.TO_UNRESOLVED -> {
                val relativize = file.downloadPath.relativize(fileDownloadPath)
                val path = relativize.parent?.let {
                    sourceSavePath.resolve(UNRESOLVED).resolve(it)
                } ?: sourceSavePath.resolve(UNRESOLVED)
                ResultWrapper(path, parse)
            }
        }
    }

    fun parse(patternVariables: PatternVariables,
              pathPattern: CorePathPattern,
              extraVariables: Map<String, Map<String, String>> = emptyMap()
    ): PathPattern.ParseResult {
        val pattern = pathPattern.pattern
        val expressions = pathPattern.expressions
        val matcher = variablePatternRegex.matcher(pattern)
        val pathBuilder = StringBuilder()
        val replacedVariables = patternVariables.variables().mapValues { entry ->
            var text = entry.value
            variableReplacers.forEach {
                val before = text
                text = it.replace(entry.key, text)
                if (before != text) {
                    log.debug("replace variable '{}' from '{}' to '{}'", entry.key, before, text)
                }
            }
            text
        }
        // TODO extraVariables也要替换
        val variableResults = mutableListOf<PathPattern.ExpressionResult>()
        var expressionIndex = 0
        val variables = mutableMapOf<String, Any>()
        variables.putAll(extraVariables)
        variables.putAll(replacedVariables)
        while (matcher.find()) {
            val expression = expressions[expressionIndex]
            val value = expression.eval(variables)
            variableResults.add(PathPattern.ExpressionResult(expression.raw, value != null || expression.isOptional()))
            if (value != null) {
                matcher.appendReplacement(pathBuilder, value)
            } else if (expression.isOptional()) {
                matcher.appendReplacement(pathBuilder, "")
            }
            expressionIndex = expressionIndex.inc()
        }
        matcher.appendTail(pathBuilder)
        return PathPattern.ParseResult(pathBuilder.toString(), variableResults)
    }

    companion object {

        private const val UNRESOLVED = "unresolved"
        private val variablePatternRegex: Pattern = Pattern.compile("\\{(.+?)}|:\\{(.+?)}")
        private val log = LoggerFactory.getLogger(ProcessingContext::class.java)
    }
}

data class ProcessingContext(
    val sourceItem: SourceItem,
    val file: RawFileContent,
    val sharedVariables: PatternVariables,
) {

    val patternVariables: PatternVariables by lazy {
        val map = mutableMapOf<String, String>()
        map["itemTitle"] = sourceItem.title
        map["itemDate"] = sourceItem.date.toLocalDate().toString()
        map["itemYear"] = sourceItem.date.year.toString()
        map["itemMonth"] = sourceItem.date.monthValue.toString()
        map["filename"] = file.fileDownloadPath.nameWithoutExtension

        map.putAll(sharedVariables.variables())
        map.putAll(file.patternVariables.getVariables())
        MapPatternVariables(map)
    }
    val extraVariables: Map<String, Map<String, String>> by lazy {
        val map = mutableMapOf<String, Map<String, String>>()
        map["item.attrs"] = sourceItem.attrs.mapValues { it.value.toString() }
        map["file.attrs"] = file.attrs.mapValues { it.value.toString() }
        map
    }
}

private data class ResultWrapper<T>(
    val value: T,
    val result: PathPattern.ParseResult,
) {

    companion object {

        fun fromFilename(name: String, result: PathPattern.ParseResult = PathPattern.ParseResult(name, emptyList())): ResultWrapper<String> {
            return ResultWrapper(name, result)
        }

        fun fromPath(path: Path, result: PathPattern.ParseResult): ResultWrapper<Path> {
            return ResultWrapper(path, result)
        }
    }
}

data class RawFileContent(
    val fileDownloadPath: Path,
    val sourceSavePath: Path,
    val downloadPath: Path,
    val patternVariables: MapPatternVariables,
    val savePathPattern: CorePathPattern,
    val filenamePattern: CorePathPattern,
    val attrs: Map<String, Any>,
    val tags: Set<String>,
    val fileUri: URI?
) {

    fun createContent(targetSavePath: Path, filename: String, errors: List<String>): CoreFileContent {
        return CoreFileContent(
            fileDownloadPath,
            sourceSavePath,
            downloadPath,
            patternVariables,
            savePathPattern,
            filenamePattern,
            targetSavePath,
            filename,
            attrs,
            tags,
            fileUri,
            errors,
        )
    }
}