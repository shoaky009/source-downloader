package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent

interface BatchFileMover : FileMover {

    fun batchMove(itemContent: ItemContent): BatchMoveResult

}