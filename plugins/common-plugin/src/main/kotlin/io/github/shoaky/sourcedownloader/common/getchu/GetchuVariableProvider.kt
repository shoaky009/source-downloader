package io.github.shoaky.sourcedownloader.common.getchu

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.jvm.optionals.getOrNull

class GetchuVariableProvider(
    private val client: GetchuClient = GetchuClient
) : VariableProvider {

    private val cache = CacheBuilder.newBuilder().maximumSize(500).build(
        object : CacheLoader<RequestKey, Optional<GetchuDetailItem>>() {
            override fun load(request: RequestKey): Optional<GetchuDetailItem> {
                return Optional.ofNullable(itemRequest(request))
            }
        })

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        val isbn = findIsbn(sourceItem)
        if (isbn != null) {
            val item = this.cache.get(RequestKey(isbn)).getOrNull()
            if (item == null) {
                log.info("No item found for isbn: {}", isbn)
                return PatternVariables.EMPTY
            }
            return item
        }

        // 后续title需要优化
        val item = this.cache.get(RequestKey(keyword = sourceItem.title)).getOrNull()
        if (item == null) {
            log.info("No item found for title: {}", sourceItem.title)
            return PatternVariables.EMPTY
        }
        return item
    }

    override fun primaryVariableName(): String {
        return "title"
    }

    private fun itemRequest(request: RequestKey): GetchuDetailItem? {
        if (request.isbn != null) {
            return searchIsbn(request.isbn)
        }
        if (request.keyword != null) {
            return searchKeyword(request.keyword)
        }
        throw IllegalArgumentException("Request key is empty")
    }

    private fun searchKeyword(keyword: String): GetchuDetailItem? {
        // 后续看情况优化
        return client.searchKeyword(keyword)
            .minByOrNull {
                it.title.length
            }?.let {
                client.itemDetail(it.url)
            }
    }

    private fun findIsbn(sourceItem: SourceItem): String? {
        return isbnRegex.find(sourceItem.title)?.value
    }

    private fun searchIsbn(isbn: String): GetchuDetailItem? {
        // isbn搜索不到结果，暂时用keyword速度慢很多
        return client.searchKeyword(isbn)
            // isbn有多个一般是不同版本有些广告语，取标题最短的
            .minByOrNull {
                it.title.length
            }?.let {
                client.itemDetail(it.url)
            }
    }

    companion object {

        private val isbnRegex = Regex("[a-zA-Z]+-[a-zA-Z0-9]+")
        private val log = LoggerFactory.getLogger(GetchuVariableProvider::class.java)
    }

    private data class RequestKey(
        val isbn: String? = null,
        val keyword: String? = null
    )
}