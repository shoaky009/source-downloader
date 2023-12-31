package io.github.shoaky.sourcedownloader.foreign.component

import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider

class ForeignFileReplacementDecider(
    private val client: ForeignStateClient,
) : FileReplacementDecider {

    override fun isReplace(current: ItemContent, before: ItemContent?, existingFile: SourceFile): Boolean {
        TODO("Not yet implemented")
    }
}