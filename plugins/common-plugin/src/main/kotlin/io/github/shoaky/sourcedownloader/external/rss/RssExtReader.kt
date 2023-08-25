package io.github.shoaky.sourcedownloader.external.rss

import com.apptasticsoftware.rssreader.AbstractRssReader
import com.apptasticsoftware.rssreader.Channel
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient

class RssExtReader : AbstractRssReader<Channel, ItemExt>(httpClient) {

    override fun createChannel(): Channel {
        return Channel()
    }

    override fun createItem(): ItemExt {
        return ItemExt()
    }

}