package xyz.shoaky.sourcedownloader.component

import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.tools.Script
import org.projectnessie.cel.tools.ScriptHost
import org.slf4j.LoggerFactory
import org.springframework.expression.BeanResolver
import org.springframework.expression.Expression
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.util.unit.DataSize
import xyz.shoaky.sourcedownloader.sdk.FileContent
import xyz.shoaky.sourcedownloader.sdk.component.FileContentFilter
import xyz.shoaky.sourcedownloader.util.creationTime
import xyz.shoaky.sourcedownloader.util.fileDataSize
import xyz.shoaky.sourcedownloader.util.lastModifiedTime
import java.nio.file.Files
import kotlin.io.path.name


class ExpressionFileFilter(
    private val exclusions: List<Expression>,
    private val inclusions: List<Expression>
) : FileContentFilter {
    override fun test(content: FileContent): Boolean {
        val path = content.fileDownloadPath
        val creationTime = path.creationTime()
        val lastModifiedTime = path.lastModifiedTime()

        content.patternVariables.variables()
        val variables = mapOf(
            "filename" to path.name,
            "size" to path.fileDataSize(),
            "contentType" to Files.probeContentType(path),
            "creationTime" to creationTime,
            "lastModifiedTime" to lastModifiedTime
        )

        val ctx = StandardEvaluationContext()
        ctx.setVariables(variables)
        ctx.setBeanResolver(dataSizeBeanResolver)
        val all = exclusions.map { it.getValue(ctx, Boolean::class.java) == true }
        if (all.isNotEmpty() && all.any { it }) {
            log.debug("File {} is excluded by expressions", path)
            return false
        }
        val any = inclusions.map { it.getValue(ctx, Boolean::class.java) == true }.all { it }
        if (any) {
            log.debug("File {} is included by expressions", path)
            return true
        }
        return false
    }

    companion object {
        private val dataSizeBeanResolver = BeanResolver { _, beanName -> DataSize.parse(beanName.removePrefix("&")) }
        private val log = LoggerFactory.getLogger(ExpressionFileFilter::class.java)
    }

}

class ExpressionFileV2(
    exclusions: List<String>,
    inclusions: List<String>
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
        return true
    }

    companion object {
        private val scriptHost = ScriptHost.newBuilder().build()
        private fun buildScript(expression: String): Script {
            return scriptHost.buildScript(expression)
                .withDeclarations(
                    Decls.newVar("filename", Decls.String),
                    Decls.newVar("tags", Decls.newListType(Decls.String)),
                    Decls.newVar("extension", Decls.String),
                    // short for patternVariables
                    Decls.newVar("pv", Decls.newMapType(Decls.String, Decls.String)),
                )
                .build()
        }
    }
}