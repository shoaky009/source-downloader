package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.core.expression.*
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import org.slf4j.LoggerFactory

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
    expressionFactory: CompiledExpressionFactory = CelCompiledExpressionFactory
) : SourceItemFilter {

    private val exclusionScripts: List<CompiledExpression<Boolean>> by lazy {
        exclusions.map {
            expressionFactory.create(it, Boolean::class.java, sourceItemDefs())
        }
    }
    private val inclusionScripts: List<CompiledExpression<Boolean>> by lazy {
        inclusions.map {
            expressionFactory.create(it, Boolean::class.java, sourceItemDefs())
        }
    }

    override fun test(item: SourceItem): Boolean {
        val all = exclusionScripts.map { it.execute(item.variables()) }
        if (all.isNotEmpty() && all.any { it }) {
            log.debug("Item {} is excluded by expressions", item)
            return false
        }
        val any = inclusionScripts.map { it.execute(item.variables()) }.all { it }
        if (any) {
            log.debug("Item {} is included by expressions", item)
            return true
        }
        return false
    }

    companion object {

        private val log = LoggerFactory.getLogger(ExpressionItemFilter::class.java)

    }
}