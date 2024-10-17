package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.core.ProcessingContent

data class Scroll(
    val contents: List<ProcessingContent>,
    val nextMaxId: Long
)