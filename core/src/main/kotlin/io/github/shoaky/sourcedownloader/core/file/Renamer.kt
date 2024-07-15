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
    private val variableProcessChain: List<VariableProcessChain> = emptyList()
) {

    fun createFileContent(
        file: RawFileContent,
        extraVariables: RenameVariables,
    ): CoreFileContent {
        val variables = fileRenameVariables(file, extraVariables)
        val dirResult = saveDirectoryPath(file, variables)
        val filenameResult = targetFilename(file, variables)
        val errors = mutableListOf<String>()
        errors.addAll(dirResult.result.failedExpression())
        errors.addAll(filenameResult.result.failedExpression())

        if (filenameResult.result.success().not() && variableErrorStrategy == VariableErrorStrategy.STAY) {
            val fileDownloadPath = file.fileDownloadPath
            return file.createContent(
                fileDownloadPath.parent, fileDownloadPath.name, errors, variables.processedVariables
            )
        }
        return file.createContent(dirResult.value, filenameResult.value, errors, variables.processedVariables)
    }

    private fun targetFilename(file: RawFileContent, variables: RenameVariables): ResultWrapper<String> {
        val fileDownloadPath = file.fileDownloadPath
        val filenamePattern = file.filenamePattern
        if (filenamePattern == CorePathPattern.origin) {
            return ResultWrapper.fromFilename(fileDownloadPath.name)
        }
        val parse = parse(variables.allVariables, filenamePattern)
        val success = parse.success()
        if (success) {
            val targetFilename = parse.path
            if (targetFilename.isBlank()) {
                return ResultWrapper.fromFilename(fileDownloadPath.name, parse)
            }
            val extension = commonExtensionRegex.find(fileDownloadPath.name)?.groupValues?.lastOrNull()
            if (extension == null || targetFilename.endsWith(extension)) {
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

    private fun saveDirectoryPath(file: RawFileContent, variables: RenameVariables): ResultWrapper<Path> {
        val fileSavePathPattern = file.savePathPattern
        val sourceSavePath = file.sourceSavePath
        val fileDownloadPath = file.fileDownloadPath
        val parse = parse(variables.allVariables, fileSavePathPattern)
        if (parse.success()) {
            if (VariableErrorStrategy.TO_UNRESOLVED == variableErrorStrategy) {
                val success = parse(variables.allVariables, file.filenamePattern).success()
                if (success.not()) {
                    return ResultWrapper(sourceSavePath.resolve(parse.path).resolve(UNRESOLVED), parse)
                }
            }
            return ResultWrapper(sourceSavePath.resolve(parse.path), parse)
        }

        return when (variableErrorStrategy) {
            VariableErrorStrategy.ORIGINAL, VariableErrorStrategy.STAY -> {
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

    fun itemRenameVariables(sourceItem: SourceItem, itemVariables: PatternVariables): RenameVariables {
        val vars = mutableMapOf<String, Any>()
        val replacedItemVars = itemVariables.variables().replaceVariables()
        vars.putAll(replacedItemVars)
        vars["item"] = buildSourceItemRenameVariables(sourceItem)
        val (variables, _) = processVariable(vars, false)
        return RenameVariables(vars, variables.replaceVariables(), replacedItemVars)
    }

    private fun buildSourceItemRenameVariables(sourceItem: SourceItem): Map<String, Any> {
        return mapOf(
            "title" to sourceItem.title.replaceVariable("item.title"),
            "date" to sourceItem.datetime.toLocalDate().toString().replaceVariable("item.date"),
            "year" to sourceItem.datetime.year.toString().replaceVariable("item.year"),
            "month" to sourceItem.datetime.monthValue.toString().replaceVariable("item.month"),
            "contentType" to sourceItem.contentType.replaceVariable("item.contentType"),
            "attrs" to sourceItem.attrs.mapValues { it.value.toString() }.replaceVariables()
        )
    }

    /**
     * @param extraVariables 优先级比file低
     */
    private fun fileRenameVariables(file: RawFileContent, extraVariables: RenameVariables): RenameVariables {
        val renameVars = mutableMapOf<String, Any>()
        val filePatternVars = file.patternVariables.variables().replaceVariables()
        renameVars.putAll(filePatternVars)

        renameVars["file"] = buildSourceFileRenameVariables(file)
        val (variables, processed) = processVariable(renameVars)
        if (processed) {
            for ((key, value) in extraVariables.processedVariables.entries) {
                variables.putIfAbsent(key, value)
            }
        }

        for ((key, value) in extraVariables.variables.entries) {
            renameVars.putIfAbsent(key, value)
        }

        // 提供要表达式vars?.xxx的语句
        renameVars["vars"] = filePatternVars + extraVariables.patternVariables
        return RenameVariables(renameVars, variables)
    }

    private fun buildSourceFileRenameVariables(file: RawFileContent): Map<String, Any> {
        return mapOf(
            "name" to file.fileDownloadPath.nameWithoutExtension.replaceVariable("file.name"),
            "attrs" to file.sourceFile.attrs.mapValues { it.value.toString() }.replaceVariables(),
            "originalLayout" to file.getPathOriginalLayout()
                .joinToString("/") { it.replaceVariable("file.originalLayout") }
        )
    }

    /**
     * @return processed variables and whether processed
     */
    private fun processVariable(
        variables: Map<String, Any>, withCondition: Boolean = true
    ): Pair<MutableMap<String, String>, Boolean> {
        if (variableProcessChain.isEmpty()) {
            return mutableMapOf<String, String>() to false
        }
        val processedVariables: MutableMap<String, String> = mutableMapOf()
        val doc = JsonPath.parse(variables)
        var processedFlag = false
        variableProcessChain.filter {
            if (withCondition) {
                return@filter it.condition?.execute(variables) ?: true
            }
            true
        }.forEach { process ->
            processedFlag = true
            val value = try {
                doc.read<String>("$.${process.input}")
            } catch (e: PathNotFoundException) {
                log.debug("Variable '{}' not found, msg:{}", process.input, e.message)
                return@forEach
            }
            log.debug("Process variable '{}' with value '{}'", process.input, value)
            val processed = process.process(value, variables)
            if (processed.isNotEmpty()) {
                processedVariables.putAll(processed)
                for ((key, processedValue) in processed) {
                    doc.put("$", key, processedValue)
                }
            }
        }
        log.debug("Rename variables:{}", variables)
        return processedVariables to processedFlag
    }

    companion object {

        private const val UNRESOLVED = "unresolved"
        private val expressionPatternRegex: Pattern = Pattern.compile("\\{(.+?)}|:\\{(.+?)}")
        private val log = LoggerFactory.getLogger(Renamer::class.java)

        // 不一定完全正确现实情况比较复杂使用该正则过滤部分情况
        private val commonExtensionRegex = Regex(".([a-zA-Z0-9]{1,10}\$)")

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

private data class ResultWrapper<T>(
    val value: T,
    val result: PathPattern.ParseResult,
) {

    companion object {

        fun fromFilename(
            name: String, result: PathPattern.ParseResult = PathPattern.ParseResult(name, emptyList())
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
        return path.normalize().drop(1).dropLast(1).map {
            it.name
        }
    }

    fun createContent(
        targetSavePath: Path, filename: String, errors: List<String>, processedVariables: Map<String, String>
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