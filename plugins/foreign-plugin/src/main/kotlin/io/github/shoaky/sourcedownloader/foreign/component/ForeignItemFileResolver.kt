package io.github.shoaky.sourcedownloader.foreign.component

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver

class ForeignItemFileResolver(
    private val client: ForeignStateClient,
    private val path: String
) : ItemFileResolver {

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        return client.postState(
            path,
            mapOf("sourceItem" to sourceItem),
            jacksonTypeRef()
        )
    }
}