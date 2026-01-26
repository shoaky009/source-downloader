package io.github.shoaky.sourcedownloader.foreign.component

import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.ItemContentFilter

class ForeignItemContentFilter(
    private val client: ForeignStateClient,
) : ItemContentFilter {

    override fun test(content: ItemContent): Boolean {
        TODO("Not yet implemented")
    }
}