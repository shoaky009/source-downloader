package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.util.scriptHost
import org.projectnessie.cel.checker.Decls

interface SourceItemPartition {

    fun match(item: SourceItem): Boolean
}

class ExpressionSourceItemPartition(
    expression: String
) : SourceItemPartition {

    private val script = scriptHost.buildScript(expression).withDeclarations(
        Decls.newVar("title", Decls.String),
        Decls.newVar("contentType", Decls.String),
        Decls.newVar("link", Decls.String),
        Decls.newVar("downloadUri", Decls.String),
        Decls.newVar("date", Decls.Timestamp),
        Decls.newVar("tags", Decls.newListType(Decls.String)),
        Decls.newVar("attrs", Decls.newMapType(Decls.String, Decls.Dyn))
    ).build()

    override fun match(item: SourceItem): Boolean {
        // 后面再调整
        // script.execute(
        //     Boolean::class.java, mapOf(
        //         "item" to item,
        //     )
        // )
        return script.execute(
            Boolean::class.java, mapOf(
                "title" to item.title,
                "contentType" to item.contentType,
                "link" to item.link,
                "downloadUri" to item.downloadUri,
                "date" to item.date,
                "tags" to item.tags.toList(),
                "attrs" to item.attrs,
            )
        )
    }
}

class TagSourceItemPartition(
    private val tags: Set<String>
) : SourceItemPartition {

    override fun match(item: SourceItem): Boolean {
        return item.tags.containsAll(tags)
    }
}