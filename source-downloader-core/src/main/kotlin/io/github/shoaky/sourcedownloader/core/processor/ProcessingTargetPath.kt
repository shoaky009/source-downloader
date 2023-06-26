package io.github.shoaky.sourcedownloader.core.processor

import java.nio.file.Path

data class ProcessingTargetPath(
    val targetPaths: List<Path>,
    val processorName: String? = null,
    val itemHashing: String? = null,
)