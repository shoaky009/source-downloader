package io.github.shoaky.sourcedownloader.foreign.component

import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.ProcessContext
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ProcessListener

class ForeignProcessListener(
    private val client: ForeignStateClient,
) : ProcessListener {

    override fun onItemError(context: ProcessContext, sourceItem: SourceItem, throwable: Throwable) {
        super.onItemError(context, sourceItem, throwable)
    }

    override fun onItemSuccess(context: ProcessContext, itemContent: ItemContent) {
        super.onItemSuccess(context, itemContent)
    }

    override fun onProcessCompleted(processContext: ProcessContext) {
        super.onProcessCompleted(processContext)
    }
}