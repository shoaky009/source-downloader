package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.component.FileContentFilter
import io.github.shoaky.sourcedownloader.util.CelLibrary
import io.github.shoaky.sourcedownloader.util.scriptHost
import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.tools.Script
import org.slf4j.LoggerFactory
import kotlin.io.path.extension
import kotlin.io.path.name


class ExpressionFileFilter(
    exclusions: List<String> = emptyList(),
    inclusions: List<String> = emptyList()
) : FileContentFilter {

    private val exclusionScripts: List<Script> by lazy {
        exclusions.map {
            buildScript(it)
        }
    }
    private val inclusionScripts: List<Script> by lazy {
        inclusions.map { buildScript(it) }
    }

    override fun test(content: FileContent): Boolean {
        val paths = content.fileDownloadRelativeParentDirectory()?.toList()?.map { it.name } ?: emptyList()
        val variables = mapOf(
            "filename" to content.fileDownloadPath.name,
            "tags" to content.tags.toList(),
            "ext" to content.fileDownloadPath.extension.lowercase(),
            "vars" to content.patternVariables.variables(),
            "attr" to content.attributes,
            "paths" to paths
        )

        val all = exclusionScripts.map { it.execute(Boolean::class.java, variables) == true }
        if (all.isNotEmpty() && all.any { it }) {
            log.debug("File {} is excluded by expression", content.fileDownloadPath)
            return false
        }

        val any = inclusionScripts.map { it.execute(Boolean::class.java, variables) == true }.all { it }
        if (any) {
            log.debug("File {} is included by expressions", content.fileDownloadPath)
            return true
        }
        return false
    }

    companion object {

        private val log = LoggerFactory.getLogger(ExpressionFileFilter::class.java)
        private fun buildScript(expression: String): Script {
            return scriptHost.buildScript(expression)
                .withDeclarations(
                    Decls.newVar("filename", Decls.String),
                    Decls.newVar("tags", Decls.newListType(Decls.String)),
                    // abbr. extension
                    Decls.newVar("ext", Decls.String),
                    // abbr. patternVariables
                    Decls.newVar("vars", Decls.newMapType(Decls.String, Decls.String)),
                    Decls.newVar("attr", Decls.newMapType(Decls.String, Decls.Any)),
                    Decls.newVar("paths", Decls.newListType(Decls.String)),
                )
                .withLibraries(CelLibrary())
                .build()
        }
    }
}