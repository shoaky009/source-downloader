package io.github.shoaky.sourcedownloader.foreign.component

import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.SourceItemFilter

class ForeignSourceItemFilter(
    private val client: ForeignStateClient,
) : SourceItemFilter {

    override fun test(t: SourceItem): Boolean {
        TODO("Not yet implemented")
    }
}