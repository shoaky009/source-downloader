package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.util.scriptHost
import org.projectnessie.cel.checker.Decls
import kotlin.io.path.name

interface SourceFilePartition {

    fun match(sourceFile: SourceFile): Boolean

}

class ExpressionSourceFilePartition(
    expression: String
) : SourceFilePartition {

    private val script = scriptHost.buildScript(expression).withDeclarations(
        Decls.newVar("filename", Decls.Dyn),
        Decls.newVar("tags", Decls.newListType(Decls.String)),
        Decls.newVar("attrs", Decls.newMapType(Decls.String, Decls.Dyn))
    ).build()

    override fun match(sourceFile: SourceFile): Boolean {

        return script.execute(
            Boolean::class.java, mapOf(
                "filename" to sourceFile.path.name,
                "tags" to sourceFile.tags,
                "attrs" to sourceFile.attrs,
            )
        )
    }
}

class TagSourceFilePartition(
    private val tags: Set<String>
) : SourceFilePartition {

    override fun match(sourceFile: SourceFile): Boolean {
        return sourceFile.tags.containsAll(tags)
    }
}