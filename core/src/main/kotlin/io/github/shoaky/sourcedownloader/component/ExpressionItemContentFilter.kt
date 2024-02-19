package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.core.expression.*
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.ItemContentFilter
import org.slf4j.LoggerFactory

/**
 * Item级别的CEL表达式过滤器，可用变量有
 * title:Item中的title
 * contentType:Item中的contentType
 * link:Item中的link
 * date:日期
 * attrs:文件额外属性key-value例如{"name":"demo"}
 * tags:标签，数组类型例如["demo"]
 * files:Item中所解析出来的文件，数组类型例如[{"tags":["demo"],"attrs":{"name":"demo"},"vars":{"name":"demo"}}]
 */
class ExpressionItemContentFilter(
    exclusions: List<String> = emptyList(),
    inclusions: List<String> = emptyList(),
    expressionFactory: CompiledExpressionFactory = CelCompiledExpressionFactory
) : ItemContentFilter {

    private val exclusionScripts: List<CompiledExpression<Boolean>> = exclusions.map {
        expressionFactory.create(it, Boolean::class.java, itemContentDefs())
    }
    private val inclusionScripts: List<CompiledExpression<Boolean>> = inclusions.map {
        expressionFactory.create(it, Boolean::class.java, itemContentDefs())
    }

    override fun test(content: ItemContent): Boolean {
        val item = content.sourceItem
        val all = exclusionScripts.map { it.execute(content.variables()) }
        if (all.isNotEmpty() && all.any { it }) {
            log.debug("ItemContent {} is excluded by expressions", item)
            return false
        }
        val any = inclusionScripts.map { it.execute(content.variables()) }.all { it }
        if (any) {
            log.debug("ItemContent {} is included by expressions", item)
            return true
        }
        return false
    }

    companion object {

        private val log = LoggerFactory.getLogger(ExpressionItemContentFilter::class.java)
    }


}