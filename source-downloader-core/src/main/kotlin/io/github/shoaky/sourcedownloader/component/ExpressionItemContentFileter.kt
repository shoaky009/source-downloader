package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.ItemContentFilter
import io.github.shoaky.sourcedownloader.util.scriptHost
import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.tools.Script
import org.slf4j.LoggerFactory

class ExpressionItemContentFileter(
    exclusions: List<String> = emptyList(),
    inclusions: List<String> = emptyList(),
) : ItemContentFilter {

    private val exclusionScripts: List<Script> by lazy {
        exclusions.map {
            buildScript(it)
        }
    }
    private val inclusionScripts: List<Script> by lazy {
        inclusions.map { buildScript(it) }
    }

    override fun test(content: ItemContent): Boolean {
        val item = content.sourceItem
        val itemVars = bindItemScriptVars(item)
        val sourceFiles = content.sourceFiles
        val mutableMapOf = mutableMapOf<String, Any>(
            "files" to sourceFiles.map {
                mapOf(
                    "tags" to it.tags.toList(),
                    "attrs" to it.attributes,
                    "vars" to it.patternVariables.variables()
                )
            }
        )
        mutableMapOf.putAll(itemVars)

        val all = exclusionScripts.map { it.execute(Boolean::class.java, mutableMapOf) == true }
        if (all.isNotEmpty() && all.any { it }) {
            log.debug("Item {} is excluded by expressions", item)
            return false
        }
        val any = inclusionScripts.map { it.execute(Boolean::class.java, mutableMapOf) == true }.all { it }
        if (any) {
            log.debug("Item {} is included by expressions", item)
            return true
        }
        return false
    }

    companion object {

        private val log = LoggerFactory.getLogger(ExpressionItemContentFileter::class.java)
        private fun buildScript(expression: String): Script {
            return scriptHost.buildScript(expression)
                .withDeclarations(
                    Decls.newVar("title", Decls.String),
                    Decls.newVar("contentType", Decls.newListType(Decls.String)),
                    Decls.newVar("link", Decls.String),
                    Decls.newVar("date", Decls.Timestamp),
                    Decls.newVar("tags", Decls.newListType(Decls.String)),
                    Decls.newVar("attrs", Decls.newMapType(Decls.String, Decls.Any)),
                    Decls.newVar("files", Decls.newListType(Decls.Any))
                )
                .build()
        }
    }


}