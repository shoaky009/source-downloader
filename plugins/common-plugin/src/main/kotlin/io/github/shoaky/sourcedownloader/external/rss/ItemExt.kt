package io.github.shoaky.sourcedownloader.external.rss

import com.apptasticsoftware.rssreader.DateTimeParser
import com.apptasticsoftware.rssreader.Item

class ItemExt(dateTimeParser: DateTimeParser) : Item(dateTimeParser) {

    val tags: MutableSet<String> = mutableSetOf()
    val attrs: MutableMap<String, Any> = mutableMapOf()

}