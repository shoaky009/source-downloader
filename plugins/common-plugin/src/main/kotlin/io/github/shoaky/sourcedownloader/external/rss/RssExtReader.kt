package io.github.shoaky.sourcedownloader.external.rss

import com.apptasticsoftware.rssreader.AbstractRssReader
import com.apptasticsoftware.rssreader.Channel
import com.apptasticsoftware.rssreader.DateTimeParser
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient

class RssExtReader : AbstractRssReader<Channel, ItemExt>(httpClient) {

    override fun createChannel(dateTimeParser: DateTimeParser): Channel {
        return Channel(dateTimeParser)
    }

    override fun createItem(dateTimeParser: DateTimeParser): ItemExt {
        return ItemExt(dateTimeParser)
    }

}