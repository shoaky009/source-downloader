package io.github.shoaky.sourcedownloader.component

import com.google.protobuf.Timestamp
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import io.github.shoaky.sourcedownloader.util.scriptHost
import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.tools.Script
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneOffset


class ExpressionItemFilter(
    exclusions: List<String>,
    inclusions: List<String>
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
        val instant: Instant = item.date.toInstant(ZoneOffset.UTC)
        val variables = mapOf(
            "title" to item.title,
            "contentType" to item.contentType,
            "date" to Timestamp.newBuilder()
                .setSeconds(instant.epochSecond)
                .setNanos(instant.nano)
                .build(),
            "link" to item.link
        )

        val all = exclusionScripts.map { it.execute(Boolean::class.java, variables) == true }
        if (all.isNotEmpty() && all.any { it }) {
            log.debug("Item {} is excluded by expressions", item)
            return false
        }
        val any = inclusionScripts.map { it.execute(Boolean::class.java, variables) == true }.all { it }
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
                )
                .build()
        }
    }

}

