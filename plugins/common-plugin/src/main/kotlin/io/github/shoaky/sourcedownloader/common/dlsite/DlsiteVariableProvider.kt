package io.github.shoaky.sourcedownloader.common.dlsite

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.external.dlsite.DlsiteClient
import io.github.shoaky.sourcedownloader.external.dlsite.DlsiteWorkInfo
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import org.jsoup.HttpStatusException
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * 通过RJ号获取DLsite的作品信息，SourceItem中必须有RJ号相关的内容
 */
class DlsiteVariableProvider(
    private val dlistClient: DlsiteClient = DlsiteClient(),
    private val locale: String = "ja-jp",
    private val onlyExtractId: Boolean = false,
    private val searchWorkTypeCategories: List<String> = emptyList(),
    private val preferSuggest: Boolean = true,
) : VariableProvider {

    private val cache = CacheBuilder.newBuilder().maximumSize(500).build(
        object : CacheLoader<WorkRequest, Optional<DlsiteWorkInfo>>() {
            override fun load(workRequest: WorkRequest): Optional<DlsiteWorkInfo> {
                val workInfo = requestWork(workRequest)
                return Optional.ofNullable(workInfo)
            }
        })

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        val dlsiteId = parseDlsiteId(sourceItem)
        if (onlyExtractId && dlsiteId == null) {
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
        if (preferSuggest) {
            val response = dlistClient.suggestWork(keyword, locale)
            if (response.work.isNotEmpty()) {
                val work = response.work.first()
                return fromDlsiteId(work.workNo, work.workName)
            }
        }
        val items = dlistClient.searchWork(keyword, locale, searchWorkTypeCategories)
        if (items.isNotEmpty()) {
            // 后续需要对搜索结果进行处理，先观察
            return fromDlsiteId(items.first().dlsiteId)
        }

        val response = dlistClient.suggestWork(keyword, locale)
        if (response.work.isNotEmpty()) {
            val work = response.work.first()
            log.info("Suggest work found for keyword: {}, work:{}", keyword, work)
            return fromDlsiteId(work.workNo, work.workName)
        }
        log.info("No work found for keyword: {}", keyword)
        return null
    }

    /**
     * suggest的名称暂时没有和谐优先使用
     */
    private fun fromDlsiteId(dlsiteId: String, suggestWorkName: String? = null): DlsiteWorkInfo {
        val info = try {
            dlistClient.getWorkInfo(dlsiteId, locale)
        } catch (e: HttpStatusException) {
            if (e.statusCode == 404) {
                return DlsiteWorkInfo(dlsiteId)
            }
            throw e
        }
        if (suggestWorkName == null) return info
        return info.copy(title = suggestWorkName)
    }

    override fun extractFrom(sourceItem: SourceItem, text: String): PatternVariables? {
        val dlsiteId = DlsiteClient.parseDlsiteId(text)
        if (dlsiteId == null && onlyExtractId) {
            return null
        }
        if (dlsiteId != null) {
            return cache.get(WorkRequest(dlsiteId = dlsiteId)).getOrNull()
        }
        return cache.get(WorkRequest(keyword = text)).getOrNull()
    }

    override fun primaryVariableName(): String {
        return "title"
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