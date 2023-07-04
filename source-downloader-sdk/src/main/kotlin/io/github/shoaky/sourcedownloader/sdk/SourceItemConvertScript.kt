package io.github.shoaky.sourcedownloader.sdk

/**
 * e.g.
 * titleExtract = windows.document.querySelector("title").innerText
 * dateExtract = windows.document.querySelector("date").innerText
 * contentTypeExtract = windows.document.querySelector("content-type").innerText
 * downloadUriExtract = document.querySelector("download-uri']").href
 * linkExtract = window.location.href
 */
data class SourceItemConvertScript(
    val titleExtract: String,
    val linkExtract: String,
    val dateExtract: String,
    val contentTypeExtract: String,
    val downloadUriExtract: String,
    val attributesExtract: String = "{}",
    val tagsExtract: String = "[]",
)