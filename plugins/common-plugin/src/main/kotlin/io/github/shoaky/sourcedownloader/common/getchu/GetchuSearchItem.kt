package io.github.shoaky.sourcedownloader.common.getchu

import org.jsoup.nodes.Element

data class GetchuSearchItem(
    val element: Element,
    val title: String,
    val url: String
)