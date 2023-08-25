package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import io.github.shoaky.sourcedownloader.sdk.SourceItem

data class FileProcessingContext(
    val sourceItem: SourceItem,
    val variableErrorStrategy: VariableErrorStrategy
)