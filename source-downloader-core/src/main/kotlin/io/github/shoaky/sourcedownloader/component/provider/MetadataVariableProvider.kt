package io.github.shoaky.sourcedownloader.component.provider

import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

object MetadataVariableProvider : VariableProvider {
    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return MetadataSourceItemGroup(sourceItem)
    }

    override fun support(item: SourceItem): Boolean = true

    override val accuracy: Int = 3
}

class MetadataSourceItemGroup(val sourceItem: SourceItem) : SourceItemGroup {
    override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
        val length = paths.size.toString().length
        return paths.mapIndexed { index, sf ->
            val path = sf.path
            val vars = MapPatternVariables()
            vars.addVariable("filename", path.nameWithoutExtension)
            vars.addVariable("extension", path.extension)
            vars.addVariable("sourceItemTitle", sourceItem.title)
            vars.addVariable("sourceItemDate", sourceItem.date.toLocalDate().toString())
            vars.addVariable("sequence", "${index + 1}".padStart(length, '0'))
            path.parent?.apply {
                vars.addVariable("parent", path.parent.name)
            }
            UniversalFileVariable(vars)
        }
    }

}

