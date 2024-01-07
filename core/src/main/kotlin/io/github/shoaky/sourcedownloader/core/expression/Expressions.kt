package io.github.shoaky.sourcedownloader.core.expression

import com.google.protobuf.Timestamp
import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import java.time.ZoneOffset
import kotlin.io.path.extension
import kotlin.io.path.name

fun sourceItemDefs(): Map<String, VariableType> {
    return mapOf(
        "title" to VariableType.STRING,
        "contentType" to VariableType.STRING,
        "date" to VariableType.DATE,
        "link" to VariableType.STRING,
        "tags" to VariableType.ARRAY,
        "attrs" to VariableType.MAP
    )
}

fun SourceItem.variables(): Map<String, Any> {
    val instant = this.date.toInstant(ZoneOffset.UTC)
    val vars = mapOf(
        "title" to this.title,
        "contentType" to this.contentType,
        "date" to Timestamp.newBuilder()
            .setSeconds(instant.epochSecond)
            .setNanos(instant.nano)
            .build(),
        "link" to this.link,
        "tags" to this.tags.toList(),
        "attrs" to this.attrs
    )
    return vars
}

fun sourceFileDefs(): Map<String, VariableType> {
    return mapOf(
        "filename" to VariableType.STRING,
        "tags" to VariableType.ARRAY,
        "attrs" to VariableType.MAP
    )
}

fun SourceFile.variables(): Map<String, Any> {
    return mapOf(
        "filename" to this.path.name,
        "tags" to this.tags,
        "attrs" to this.attrs,
    )
}

fun fileContentDefs(): Map<String, VariableType> {
    return mapOf(
        "filename" to VariableType.STRING,
        "tags" to VariableType.ARRAY,
        // abbr. extension
        "ext" to VariableType.STRING,
        // abbr. patternVariables
        "vars" to VariableType.MAP,
        "attrs" to VariableType.MAP,
        "paths" to VariableType.ARRAY,
    )
}

fun FileContent.variables(): Map<String, Any> {
    val paths = this.fileDownloadRelativeParentDirectory()?.toList()?.map { it.name } ?: emptyList()
    return mapOf(
        "filename" to this.fileDownloadPath.name,
        "tags" to this.tags.toList(),
        "ext" to this.fileDownloadPath.extension.lowercase(),
        "vars" to this.patternVariables.variables(),
        "attrs" to this.attrs,
        "paths" to paths
    )
}

fun itemContentDefs(): Map<String, VariableType> {
    return mapOf(
        "title" to VariableType.STRING,
        "contentType" to VariableType.STRING,
        "link" to VariableType.STRING,
        "date" to VariableType.DATE,
        "tags" to VariableType.ARRAY,
        "attrs" to VariableType.MAP,
        "vars" to VariableType.MAP,
        // TODO 范型定义
        "files" to VariableType.ARRAY
    )
}

fun ItemContent.variables(): Map<String, Any> {
    val itemVars = this.sourceItem.variables()
    val sourceFiles = this.sourceFiles
    val extraVars = mutableMapOf<String, Any>(
        "files" to sourceFiles.map {
            mapOf(
                "tags" to it.tags.toList(),
                "attrs" to it.attrs,
                "vars" to it.patternVariables.variables()
            )
        }
    )
    extraVars["vars"] = this.sharedPatternVariables.variables()
    extraVars.putAll(itemVars)
    return extraVars
}