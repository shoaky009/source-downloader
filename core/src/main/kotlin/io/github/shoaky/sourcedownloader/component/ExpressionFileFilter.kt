package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.core.expression.*
import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.component.FileContentFilter
import org.slf4j.LoggerFactory

/**
 * 文件级别的CEL表达式过滤器，可用变量有
 * filename:文件名不包括扩展名
 * ext:文件扩展名例如mp4
 * tags:标签，数组类型例如["demo"]
 * vars:文件对应的变量key-value例如{"name":"demo"}
 * attrs:文件额外属性key-value例如{"name":"demo"}
 * paths:下载文件中的路径，数组类型例如["FATE", "CDs"]
 */
class ExpressionFileFilter(
    exclusions: List<String> = emptyList(),
    inclusions: List<String> = emptyList(),
    expressionFactory: CompiledExpressionFactory = CelCompiledExpressionFactory
) : FileContentFilter {

    private val exclusionScripts: List<CompiledExpression<Boolean>> by lazy {
        exclusions.map {
            expressionFactory.create(it, Boolean::class.java, fileContentDefs())
        }
    }
    private val inclusionScripts: List<CompiledExpression<Boolean>> by lazy {
        inclusions.map {
            expressionFactory.create(it, Boolean::class.java, fileContentDefs())
        }
    }

    override fun test(content: FileContent): Boolean {
        val all = exclusionScripts.map { it.execute(content.variables()) }
        if (all.isNotEmpty() && all.any { it }) {
            log.debug("File {} is excluded by expression", content.fileDownloadPath)
            return false
        }

        val any = inclusionScripts.map { it.execute(content.variables()) }.all { it }
        if (any) {
            log.debug("File {} is included by expressions", content.fileDownloadPath)
            return true
        }
        return false
    }

    override fun toString(): String {
        return "ExpressionFileFilter(exclusionScripts=$exclusionScripts, inclusionScripts=$inclusionScripts)"
    }

    companion object {

        private val log = LoggerFactory.getLogger(ExpressionFileFilter::class.java)

    }
}