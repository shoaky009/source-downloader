package io.github.shoaky.sourcedownloader.component

import com.google.protobuf.Timestamp
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import io.github.shoaky.sourcedownloader.util.scriptHost
import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.tools.Script
import org.slf4j.LoggerFactory
import java.time.ZoneOffset

/**
 * Item级别的CEL表达式过滤器，可用变量有
 * title:Item中的title
 * contentType:Item中的contentType
 * link:Item中的link
 * date:日期
 * attrs:文件额外属性key-value例如{"name":"demo"}
 * tags:标签，数组类型例如["demo"]
 */
class ExpressionItemFilter(
    exclusions: List<String> = emptyList(),
    inclusions: List<String> = emptyList(),
) : SourceItemFilter {

    private val exclusionScripts: List<Script> by lazy {
        exclusions.map {
            buildScript(it)
        }
    }
    private val inclusionScripts: List<Script> by lazy {
        inclusions.map { buildScript(it) }
    }

    override fun test(item: SourceItem): Boolean {
        val itemVars = bindItemScriptVars(item)

        val all = exclusionScripts.map { it.execute(Boolean::class.java, itemVars) == true }
        if (all.isNotEmpty() && all.any { it }) {
            log.debug("Item {} is excluded by expressions", item)
            return false
        }
        val any = inclusionScripts.map { it.execute(Boolean::class.java, itemVars) == true }.all { it }
        if (any) {
            log.debug("Item {} is included by expressions", item)
            return true
        }
        return false
    }

    companion object {

        private val log = LoggerFactory.getLogger(ExpressionItemFilter::class.java)
        private fun buildScript(expression: String): Script {
            return scriptHost.buildScript(expression)
                .withDeclarations(
                    Decls.newVar("title", Decls.String),
                    Decls.newVar("contentType", Decls.newListType(Decls.String)),
                    Decls.newVar("link", Decls.String),
                    Decls.newVar("date", Decls.Timestamp),
                    Decls.newVar("tags", Decls.newListType(Decls.String)),
                    Decls.newVar("attrs", Decls.newMapType(Decls.String, Decls.Any)),
                )
                .build()
        }
    }
}

fun bindItemScriptVars(item: SourceItem): Map<String, Any> {
    val instant = item.date.toInstant(ZoneOffset.UTC)
    return mapOf(
        "title" to item.title,
        "contentType" to item.contentType,
        "date" to Timestamp.newBuilder()
            .setSeconds(instant.epochSecond)
            .setNanos(instant.nano)
            .build(),
        "link" to item.link,
        "tags" to item.tags.toList(),
        "attrs" to item.attrs
    )
}
