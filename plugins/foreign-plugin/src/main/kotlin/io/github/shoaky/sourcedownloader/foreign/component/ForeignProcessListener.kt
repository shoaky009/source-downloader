package io.github.shoaky.sourcedownloader.foreign.component

import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.ProcessContext
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ProcessListener

class ForeignProcessListener(
    private val client: ForeignStateClient,
) : ProcessListener {

    override fun onItemError(sourceItem: SourceItem, throwable: Throwable) {
        super.onItemError(sourceItem, throwable)
    }

    override fun onItemSuccess(itemContent: ItemContent) {
        super.onItemSuccess(itemContent)
    }

    override fun onProcessCompleted(processContext: ProcessContext) {
        super.onProcessCompleted(processContext)
    }
}