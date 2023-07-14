package io.github.shoaky.sourcedownloader.external.rss

import com.apptasticsoftware.rssreader.Item

class ItemExt : Item() {

    val tags: MutableSet<String> = mutableSetOf()
    val attrs: MutableMap<String, Any> = mutableMapOf()

}