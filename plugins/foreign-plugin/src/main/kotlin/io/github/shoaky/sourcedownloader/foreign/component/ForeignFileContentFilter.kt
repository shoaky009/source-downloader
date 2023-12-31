package io.github.shoaky.sourcedownloader.foreign.component

import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.component.FileContentFilter

class ForeignFileContentFilter(
    private val client: ForeignStateClient,
) : FileContentFilter {

    override fun test(t: FileContent): Boolean {
        TODO("Not yet implemented")
    }
}