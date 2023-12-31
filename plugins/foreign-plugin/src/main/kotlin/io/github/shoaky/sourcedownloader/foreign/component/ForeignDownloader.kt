package io.github.shoaky.sourcedownloader.foreign.component

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.foreign.methods.DownloaderForeignMethods
import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import java.nio.file.Path
import kotlin.io.path.Path

open class ForeignDownloader(
    private val client: ForeignStateClient,
    private val paths: DownloaderForeignMethods,
) : Downloader {

    override fun submit(task: DownloadTask): Boolean {
        val postState = client.postState(
            paths.submit,
            mapOf("task" to task),
            jacksonTypeRef<JsonNode>()
        )
        return postState.get("result").booleanValue()
    }

    override fun defaultDownloadPath(): Path {
        val postState = client.getState(
            paths.defaultDownloadPath,
            jacksonTypeRef<JsonNode>()
        )
        return Path(postState.get("result").textValue())
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        return client.postState(
            paths.cancel,
            mapOf("sourceItem" to sourceItem, "files" to files),
            jacksonTypeRef()
        )
    }
}