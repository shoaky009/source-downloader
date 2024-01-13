package io.github.shoaky.sourcedownloader.core.file

import io.github.shoaky.sourcedownloader.core.VariableReplacer
import io.github.shoaky.sourcedownloader.sdk.*
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.regex.Matcher
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
        val ctx = RenameContext(sourceItem, rawFile, group)
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

    private fun targetFilename(ctx: RenameContext): ResultWrapper<String> {
        val fileDownloadPath = ctx.file.fileDownloadPath
        val filenamePattern = ctx.file.filenamePattern
        if (filenamePattern == CorePathPattern.origin) {
            return ResultWrapper.fromFilename(fileDownloadPath.name)
        }
        val parse = parse(ctx.patternVariables, filenamePattern, ctx.extraVariables, ctx.extraListVariables)
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

    private fun saveDirectoryPath(ctx: RenameContext): ResultWrapper<Path> {
        val file = ctx.file
        val fileSavePathPattern = file.savePathPattern
        val sourceSavePath = file.sourceSavePath
        val fileDownloadPath = file.fileDownloadPath
        val parse = parse(ctx.patternVariables, fileSavePathPattern, ctx.extraVariables, ctx.extraListVariables)
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

    fun parse(
        patternVariables: PatternVariables,
        pathPattern: CorePathPattern,
        extraVariables: Map<String, Map<String, String>> = emptyMap(),
        extraListVariables: Map<String, List<String>> = emptyMap(),
    ): PathPattern.ParseResult {
        val pattern = pathPattern.pattern
        val expressions = pathPattern.expressions
        val matcher = variablePatternRegex.matcher(pattern)

        val replacedVariables = patternVariables.variables().replaceVariables()
        val replacedExtraVariables = extraVariables.mapValues { (_, value) ->
            value.replaceVariables()
        }
        val replacedListVariables = extraListVariables.mapValues { (key, value) ->
            value.joinToString("/") { it.replaceVariable(key) }
        }

        val pathBuilder = StringBuilder()
        val variableResults = mutableListOf<PathPattern.ExpressionResult>()
        var expressionIndex = 0
        val variables = mutableMapOf<String, Any>()
        variables.putAll(replacedListVariables)
        variables.putAll(replacedExtraVariables)
        variables.putAll(replacedVariables)

        if (log.isDebugEnabled) {
            log.debug("Rename variables:{}", variables)
        }
        while (matcher.find()) {
            val expression = expressions[expressionIndex]
            val value = expression.executeIgnoreError(variables)
            variableResults.add(PathPattern.ExpressionResult(expression.raw(), value != null || expression.optional()))
            if (value != null) {
                val replacement = Matcher.quoteReplacement(value)
                matcher.appendReplacement(pathBuilder, replacement)
            } else if (expression.optional()) {
                matcher.appendReplacement(pathBuilder, "")
            }
            expressionIndex = expressionIndex.inc()
        }
        matcher.appendTail(pathBuilder)
        return PathPattern.ParseResult(pathBuilder.toString(), variableResults)
    }

    private fun Map<String, String>.replaceVariables(): Map<String, String> {
        return this.mapValues { entry ->
            entry.value.replaceVariable(entry.key)
        }
    }

    private fun String.replaceVariable(name: String): String {
        var text = this
        variableReplacers.forEach {
            val before = text
            text = it.replace(name, text)
            if (before != text) {
                log.debug("replace variable '{}' from '{}' to '{}'", name, before, text)
            }
        }
        return text
    }

    companion object {

        private const val UNRESOLVED = "unresolved"
        private val variablePatternRegex: Pattern = Pattern.compile("\\{(.+?)}|:\\{(.+?)}")
        private val log = LoggerFactory.getLogger(RenameContext::class.java)

    }

}

data class RenameContext(
    val sourceItem: SourceItem,
    val file: RawFileContent,
    val sharedVariables: PatternVariables,
) {

    val patternVariables: PatternVariables by lazy {
        val map = mutableMapOf<String, String>()
        map["itemTitle"] = sourceItem.title
        map["itemDate"] = sourceItem.datetime.toLocalDate().toString()
        map["itemYear"] = sourceItem.datetime.year.toString()
        map["itemMonth"] = sourceItem.datetime.monthValue.toString()
        map["filename"] = file.fileDownloadPath.nameWithoutExtension

        map.putAll(sharedVariables.variables())
        // 文件变量的优先
        map.putAll(file.patternVariables.getVariables())
        MapPatternVariables(map)
    }
    val extraVariables: Map<String, Map<String, String>> = run {
        val map = mutableMapOf<String, Map<String, String>>()
        map["item.attrs"] = sourceItem.attrs.mapValues { it.value.toString() }
        map["file.attrs"] = file.sourceFile.attrs.mapValues { it.value.toString() }
        map
    }

    /**
     * Value会被join成字符串，分隔符/
     */
    val extraListVariables: Map<String, List<String>> = run {
        val map = mutableMapOf<String, List<String>>()
        map["originalLayout"] = file.getPathOriginalLayout()
        map
    }
}

private data class ResultWrapper<T>(
    val value: T,
    val result: PathPattern.ParseResult,
) {

    companion object {

        fun fromFilename(
            name: String,
            result: PathPattern.ParseResult = PathPattern.ParseResult(name, emptyList())
        ): ResultWrapper<String> {
            return ResultWrapper(name, result)
        }
    }
}

data class RawFileContent(
    val sourceSavePath: Path,
    val downloadPath: Path,
    val patternVariables: MapPatternVariables,
    val savePathPattern: CorePathPattern,
    val filenamePattern: CorePathPattern,
    val sourceFile: SourceFile,
) {

    val fileDownloadPath: Path = downloadPath.resolve(sourceFile.path)

    fun getPathOriginalLayout(): List<String> {
        val path = if (sourceFile.path.isAbsolute) {
            downloadPath.relativize(sourceFile.path)
        } else {
            sourceFile.path
        }
        return path.normalize().drop(1).dropLast(1)
            .map {
                it.name
            }
    }

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
            sourceFile.attrs,
            sourceFile.tags,
            sourceFile.downloadUri,
            errors,
            data = sourceFile.data
        )
    }
}