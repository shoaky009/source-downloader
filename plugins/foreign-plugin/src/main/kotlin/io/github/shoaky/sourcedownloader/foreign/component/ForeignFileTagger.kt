package io.github.shoaky.sourcedownloader.foreign.component

import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileTagger

class ForeignFileTagger(
    private val client: ForeignStateClient,
) : FileTagger {

    override fun tag(fileContent: SourceFile): String? {
        TODO("Not yet implemented")
    }
}