package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.ItemContentFilter
import io.github.shoaky.sourcedownloader.util.scriptHost
import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.tools.Script
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
) : ItemContentFilter {

    private val exclusionScripts: List<Script> by lazy {
        exclusions.map {
            buildScript(it)
        }
    }
    private val inclusionScripts: List<Script> by lazy {
        inclusions.map {
            buildScript(it)
        }
    }

    override fun test(content: ItemContent): Boolean {
        val item = content.sourceItem
        val itemVars = bindItemScriptVars(item)
        val sourceFiles = content.sourceFiles
        val extraVars = mutableMapOf<String, Any>(
            "files" to sourceFiles.map {
                mapOf(
                    "tags" to it.tags.toList(),
                    "attrs" to it.attrs,
                    "vars" to it.patternVariables.variables()
                )
            }
        )
        extraVars["vars"] = content.sharedPatternVariables.variables()
        extraVars.putAll(itemVars)

        val all = exclusionScripts.map { it.execute(Boolean::class.java, extraVars) == true }
        if (all.isNotEmpty() && all.any { it }) {
            log.debug("ItemContent {} is excluded by expressions", item)
            return false
        }
        val any = inclusionScripts.map { it.execute(Boolean::class.java, extraVars) == true }.all { it }
        if (any) {
            log.debug("ItemContent {} is included by expressions", item)
            return true
        }
        return false
    }

    companion object {

        private val log = LoggerFactory.getLogger(ExpressionItemContentFilter::class.java)
        private fun buildScript(expression: String): Script {
            return scriptHost.buildScript(expression)
                .withDeclarations(
                    Decls.newVar("title", Decls.String),
                    Decls.newVar("contentType", Decls.newListType(Decls.String)),
                    Decls.newVar("link", Decls.String),
                    Decls.newVar("date", Decls.Timestamp),
                    Decls.newVar("tags", Decls.newListType(Decls.String)),
                    Decls.newVar("attrs", Decls.newMapType(Decls.String, Decls.Dyn)),
                    Decls.newVar("vars", Decls.newMapType(Decls.String, Decls.String)),
                    Decls.newVar("files", Decls.newListType(Decls.Any))
                )
                .build()
        }
    }


}