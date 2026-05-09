package io.github.shoaky.sourcedownloader.service

data class ProcessorMetadata(
    val version: Int,
    val rootType: String,
    val fields: List<ProcessorMetadataField>,
)

data class ProcessorMetadataField(
    val path: String,
    val title: String? = null,
    val description: String? = null,
    val type: String,
    val required: Boolean = false,
    val askMode: String = "auto",
    val level: String = "basic",
    val defaultValue: Any? = null,
    val refRootType: String? = null,
    val componentTypes: List<String> = emptyList(),
    val valueType: String? = null,
    val patternKind: String? = null,
)
