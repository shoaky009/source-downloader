package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.FileContent

data class BatchMoveResult(
    val success: Boolean,
    val failed: List<FileContent>
)