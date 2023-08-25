package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.slf4j.LoggerFactory

/**
 * 根据[SourceItem]的hash值过滤已下载的内容
 * 该组件没有对应[ComponentSupplier], 内置创建
 */
class SourceHashingItemFilter(
    private val sourceName: String,
    private val processingStorage: ProcessingStorage
) : SourceItemFilter {

    override fun test(item: SourceItem): Boolean {
        val processingContent = processingStorage.findByNameAndHash(sourceName, item.hashing())
        if (processingContent != null) {
            if (log.isDebugEnabled) {
                log.debug("Source:${sourceName}已提交过下载不做处理，item:${Jackson.toJsonString(item)}")
            }
        }
        return processingContent == null
    }

    companion object {

        private val log = LoggerFactory.getLogger(SourceHashingItemFilter::class.java)
    }
}