package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ItemContentFilter
import io.github.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.slf4j.LoggerFactory

/**
 * 根据[SourceItem]的hash值过滤已下载的内容
 * 该组件没有对应[ComponentSupplier], 内置创建
 */
class SourceItemIdentityFilter(
    private val sourceName: String,
    private val processingStorage: ProcessingStorage,
) : SourceItemFilter, ItemContentFilter {

    override fun test(item: SourceItem): Boolean {
        val exists = processingStorage.existsByNameAndHash(sourceName, item.hashing())
        if (exists) {
            if (log.isDebugEnabled) {
                log.debug("Source:${sourceName}已提交过下载不做处理，item:${Jackson.toJsonString(item)}")
            }
        }
        return exists.not()
    }

    override fun test(content: ItemContent): Boolean {
        val identity = content.sourceItem.identity
        if (!identity.isNullOrBlank()) {
            return processingStorage.existsByNameAndIdentify(sourceName, identity).not()
        }
        return test(content.sourceItem)
    }

    companion object {

        private val log = LoggerFactory.getLogger(SourceItemIdentityFilter::class.java)
    }
}