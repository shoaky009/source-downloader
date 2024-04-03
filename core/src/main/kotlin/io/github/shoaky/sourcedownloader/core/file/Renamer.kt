package io.github.shoaky.sourcedownloader.core.file

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import io.github.shoaky.sourcedownloader.core.processor.VariableProcessChain
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableReplacer
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
    private val variableProcessChain: Map<String, VariableProcessChain> = emptyMap()
) {

    fun createFileContent(
        sourceItem: SourceItem,
        rawFile: RawFileContent,
        group: PatternVariables,
    ): CoreFileContent {
        val ctx = RenameContext(sourceItem, rawFile, group, variableReplacers, variableProcessChain)
        val dirResult = saveDirectoryPath(ctx)
        val filenameResult = targetFilename(ctx)
        val errors = mutableListOf<String>()
        errors.addAll(dirResult.result.failedExpression())
        errors.addAll(filenameResult.result.failedExpression())

        if (filenameResult.result.success().not() && variableErrorStrategy == VariableErrorStrategy.STAY) {
            val fileDownloadPath = rawFile.fileDownloadPath
            return rawFile.createContent(
                fileDownloadPath.parent,
                fileDownloadPath.name,
                errors,
                ctx.getProcessedVariables()
            )
        }
        return rawFile.createContent(dirResult.value, filenameResult.value, errors, ctx.getProcessedVariables())
    }

    private fun targetFilename(ctx: RenameContext): ResultWrapper<String> {
        val fileDownloadPath = ctx.file.fileDownloadPath
        val filenamePattern = ctx.file.filenamePattern
        if (filenamePattern == CorePathPattern.origin) {
            return ResultWrapper.fromFilename(fileDownloadPath.name)
        }
        val parse = parse(ctx.allVariables, filenamePattern)
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
        val parse = parse(ctx.allVariables, fileSavePathPattern)
        if (parse.success()) {
            if (VariableErrorStrategy.TO_UNRESOLVED == variableErrorStrategy) {
                val success = parse(ctx.allVariables, file.filenamePattern).success()
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

    /**
     * @param variables if value is Collection, will join with '/'
     */
    fun parse(
        variables: Map<String, Any>,
        pathPattern: CorePathPattern,
    ): PathPattern.ParseResult {
        val pattern = pathPattern.pattern
        val expressions = pathPattern.expressions
        val matcher = expressionPatternRegex.matcher(pattern)

        if (log.isDebugEnabled) {
            log.debug("Rename variables:{}", variables)
        }

        val pathBuilder = StringBuilder()
        val variableResults = mutableListOf<PathPattern.ExpressionResult>()
        var expressionIndex = 0
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

    companion object {

        private const val UNRESOLVED = "unresolved"
        private val expressionPatternRegex: Pattern = Pattern.compile("\\{(.+?)}|:\\{(.+?)}")
        private val log = LoggerFactory.getLogger(RenameContext::class.java)

    }

    data class RenameContext(
        val sourceItem: SourceItem,
        val file: RawFileContent,
        val sharedVariables: PatternVariables,
        val variableReplacers: List<VariableReplacer>,
        val variableProcessChain: Map<String, VariableProcessChain>,
    ) {
        private val processedVariables: MutableMap<String, String> = mutableMapOf()

        val allVariables: Map<String, Any> = run {
            val vars = mutableMapOf<String, Any>()
            vars.putAll(sharedVariables.variables().replaceVariables())
            // 文件变量的优先
            vars.putAll(file.patternVariables.variables().replaceVariables())
            vars["item"] = SourceItemRenameVariables(
                sourceItem.title.replaceVariable("item.title"),
                sourceItem.datetime.toLocalDate().toString().replaceVariable("item.date"),
                sourceItem.datetime.year.toString().replaceVariable("item.year"),
                sourceItem.datetime.monthValue.toString().replaceVariable("item.month"),
                sourceItem.contentType.replaceVariable("item.contentType"),
                sourceItem.attrs.mapValues { it.value.toString() }.replaceVariables()
            )
            vars["file"] = SourceFileRenameVariables(
                file.fileDownloadPath.nameWithoutExtension.replaceVariable("file.name"),
                file.sourceFile.attrs.mapValues { it.value.toString() }.replaceVariables(),
                file.getPathOriginalLayout().joinToString("/") { it.replaceVariable("file.originalLayout") }
            )

            if (variableProcessChain.isNotEmpty()) {
                val doc = JsonPath.parse(vars)
                variableProcessChain.entries.forEach { (targetKey, process) ->
                    val value = try {
                        doc.read<String>("$.$targetKey")
                    } catch (e: PathNotFoundException) {
                        return@forEach
                    }
                    log.debug("Process variable '{}' with value '{}'", targetKey, value)
                    val processed = process.process(value)
                    processedVariables[process.output] = processed
                    vars[process.output] = processed
                }
            }
            log.debug("Rename variables:{}", vars)
            vars
        }

        fun getProcessedVariables(): Map<String, String> {
            return processedVariables
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

        private fun Map<String, String>.replaceVariables(): Map<String, String> {
            return this.mapValues { entry ->
                val value = entry.value
                value.replaceVariable(entry.key)
            }
        }
    }
}

private data class SourceItemRenameVariables(
    val title: String,
    val date: String,
    val year: String,
    val month: String,
    val contentType: String,
    val attrs: Map<String, Any>
)

private data class SourceFileRenameVariables(
    val name: String,
    val attrs: Map<String, Any>,
    val originalLayout: String
)

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

    fun createContent(
        targetSavePath: Path,
        filename: String,
        errors: List<String>,
        processedVariables: Map<String, String>
    ): CoreFileContent {
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
            data = sourceFile.data,
            processedVariables = MapPatternVariables(processedVariables)
        )
    }
}