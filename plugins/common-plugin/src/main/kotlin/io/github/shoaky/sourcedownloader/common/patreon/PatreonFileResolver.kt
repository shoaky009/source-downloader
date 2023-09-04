package io.github.shoaky.sourcedownloader.common.patreon

import io.github.shoaky.sourcedownloader.external.patreon.PatreonClient
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver

class PatreonFileResolver(
    private val client: PatreonClient
) : ItemFileResolver {

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        TODO("Not yet implemented")
    }
}