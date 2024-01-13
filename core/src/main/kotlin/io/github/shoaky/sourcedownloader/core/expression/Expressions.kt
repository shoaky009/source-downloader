package io.github.shoaky.sourcedownloader.core.expression

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import java.time.Instant
import java.time.ZoneOffset
import kotlin.io.path.extension
import kotlin.io.path.name

fun sourceItemDefs(): Map<String, VariableType> {
    return mapOf(
        "item" to VariableType.ANY,
    )
}

fun SourceItem.variables(): Map<String, Any> {
    val vars = mapOf(
        "item" to SourceItemVariables(this)
    )
    return vars
}

fun sourceFileDefs(): Map<String, VariableType> {
    return mapOf(
        "file" to VariableType.ANY,
    )
}

fun SourceFile.variables(): Map<String, Any> {
    return mapOf(
        "file" to SourceFileVariables(this)
    )
}

fun fileContentDefs(): Map<String, VariableType> {
    return mapOf(
        "file" to VariableType.ANY
    )
}

fun FileContent.variables(): Map<String, Any> {
    return mapOf(
        "file" to FileContentVariables(this)
    )
}

fun itemContentDefs(): Map<String, VariableType> {
    return mapOf(
        "item" to VariableType.ANY,
    )
}

fun ItemContent.variables(): Map<String, Any> {
    return mapOf("item" to ItemContentVariables(this))
}

data class SourceItemVariables(
    val title: String,
    val datetime: Instant,
    val date: String,
    val year: Int,
    val month: Int,
    val link: String,
    val downloadUri: String,
    val contentType: String,
    val tags: List<String>,
    val attrs: Map<String, Any>
) {

    constructor(sourceItem: SourceItem) : this(
        sourceItem.title,
        sourceItem.datetime.toInstant(ZoneOffset.UTC),
        sourceItem.datetime.toLocalDate().toString(),
        sourceItem.datetime.year,
        sourceItem.datetime.month.value,
        sourceItem.link.toString(),
        sourceItem.downloadUri.toString(),
        sourceItem.contentType,
        sourceItem.tags.toList(),
        sourceItem.attrs
    )
}

data class SourceFileVariables(
    val name: String,
    val extension: String,
    val tags: List<String>,
    val attrs: Map<String, Any>
) {

    constructor(sourceItem: SourceFile) : this(
        sourceItem.path.name,
        sourceItem.path.extension,
        sourceItem.tags.toList(),
        sourceItem.attrs
    )
}

data class ItemContentVariables(
    val title: String,
    val datetime: Instant,
    val date: String,
    val year: Int,
    val month: Int,
    val link: String,
    val downloadUri: String,
    val contentType: String,
    val tags: List<String>,
    val attrs: Map<String, Any>,
    val vars: Map<String, String>,
    val files: List<FileContentVariables>
) {

    constructor(itemContent: ItemContent) : this(
        itemContent.sourceItem.title,
        itemContent.sourceItem.datetime.toInstant(ZoneOffset.UTC),
        itemContent.sourceItem.datetime.toLocalDate().toString(),
        itemContent.sourceItem.datetime.year,
        itemContent.sourceItem.datetime.month.value,
        itemContent.sourceItem.link.toString(),
        itemContent.sourceItem.downloadUri.toString(),
        itemContent.sourceItem.contentType,
        itemContent.sourceItem.tags.toList(),
        itemContent.sourceItem.attrs,
        itemContent.sharedPatternVariables.variables(),
        itemContent.sourceFiles.map { FileContentVariables(it) }
    )
}

data class FileContentVariables(
    val name: String,
    val extension: String,
    val vars: Map<String, String>,
    val tags: List<String>,
    val attrs: Map<String, Any>,
    val paths: List<String>
) {

    constructor(fileContent: FileContent) : this(
        fileContent.fileDownloadPath.name,
        fileContent.fileDownloadPath.extension,
        fileContent.patternVariables.variables(),
        fileContent.tags.toList(),
        fileContent.attrs,
        fileContent.fileDownloadRelativeParentDirectory()?.toList()?.map { it.name } ?: emptyList()
    )
}