package xyz.shoaky.sourcedownloader.component.provider

import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name

object MetadataVariableProvider : VariableProvider {
    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return MetadataSourceItemGroup(sourceItem)
    }

    override fun support(item: SourceItem): Boolean = true

    override val accuracy: Int = 2
    override val persistentVariable: Boolean = false
}

class MetadataSourceItemGroup(val sourceItem: SourceItem) : SourceItemGroup {
    override fun sourceFiles(paths: List<Path>): List<SourceFile> {
        val length = paths.size.toString().length
        return paths.mapIndexed { index, sf ->
            val vars = MapPatternVariables()
            vars.addVariable("filename", sf.name)
            vars.addVariable("extension", sf.extension)
            vars.addVariable("sourceItemTitle", sourceItem.title)
            vars.addVariable("sourceItemDate", sourceItem.date.toLocalDate().toString())
            vars.addVariable("sequence", "${index + 1}".padStart(length, '0'))
            vars.addVariable("parent", sf.parent.name)
            UniversalSourceFile(vars)
        }
    }

}

