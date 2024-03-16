package io.github.shoaky.sourcedownloader.common.dlsite

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.external.dlsite.DlsiteClient
import io.github.shoaky.sourcedownloader.external.dlsite.DlsiteWorkInfo
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * 通过RJ号获取DLsite的作品信息，SourceItem中必须有RJ号相关的内容
 */
class DlsiteVariableProvider(
    private val dlistClient: DlsiteClient = DlsiteClient(),
    private val locale: String = "zh-cn",
    private val idOnly: Boolean = true,
) : VariableProvider {

    private val cache = CacheBuilder.newBuilder().maximumSize(500).build(
        object : CacheLoader<WorkRequest, Optional<DlsiteWorkInfo>>() {
            override fun load(workRequest: WorkRequest): Optional<DlsiteWorkInfo> {
                val workInfo = requestWork(workRequest)
                return Optional.ofNullable(workInfo)
            }
        })

    override fun itemSharedVariables(sourceItem: SourceItem): PatternVariables {
        val dlsiteId = parseDlsiteId(sourceItem)
        if (dlsiteId == null && idOnly) {
            return PatternVariables.EMPTY
        }
        if (dlsiteId != null) {
            val work = cache.get(WorkRequest(dlsiteId)).getOrNull()
            if (work == null) {
                log.info("No work found for dlsiteId: {}", dlsiteId)
                return PatternVariables.EMPTY
            }
            return work
        }

        // 后续需要对关键词进行处理
        val req = WorkRequest(keyword = sourceItem.title)
        return cache.get(req).getOrNull() ?: PatternVariables.EMPTY
    }

    private fun requestWork(
        request: WorkRequest
    ): DlsiteWorkInfo? {
        if (request.dlsiteId != null) {
            return fromDlsiteId(request.dlsiteId)
        }
        if (request.keyword == null) {
            throw IllegalArgumentException("dlsiteId and keyword can't be null at the same time")
        }
        return fromKeyword(request.keyword)
    }

    private fun fromKeyword(keyword: String): DlsiteWorkInfo? {
        val items = dlistClient.searchWork(keyword, locale)
        if (items.isEmpty()) {
            log.info("No work found for keyword: {}", keyword)
            return null
        }
        // 后续需要对搜索结果进行处理，先观察
        return fromDlsiteId(items.first().dlsiteId)
    }

    private fun fromDlsiteId(dlsiteId: String): DlsiteWorkInfo {
        return dlistClient.getWorkInfo(dlsiteId, locale)
    }

    override fun support(sourceItem: SourceItem): Boolean {
        if (idOnly.not()) {
            return true
        }
        return parseDlsiteId(sourceItem) != null
    }

    override fun extractFrom(text: String): String? {
        val dlsiteId = DlsiteClient.parseDlsiteId(text)
        if (dlsiteId == null && idOnly) {
            return null
        }

        return cache.get(WorkRequest(dlsiteId = dlsiteId)).getOrNull()?.title
            ?: cache.get(WorkRequest(keyword = text)).getOrNull()?.title
    }

    companion object {

        private val log = LoggerFactory.getLogger(DlsiteVariableProvider::class.java)
        private fun parseDlsiteId(item: SourceItem): String? {
            return DlsiteClient.parseDlsiteId(item.link.toString())
        }
    }

    private data class WorkRequest(
        val dlsiteId: String? = null,
        val keyword: String? = null
    )

}