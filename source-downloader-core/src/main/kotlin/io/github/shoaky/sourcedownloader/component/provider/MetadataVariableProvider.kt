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

class MetadataSourceItemGroup(
    val sourceItem: SourceItem
) : SourceItemGroup {

    override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
        val length = paths.size.toString().length
        return paths.mapIndexed { index, sf ->
            val path = sf.path
            val vars = MapPatternVariables()
            vars.addVariable("filename", path.nameWithoutExtension)
            vars.addVariable("extension", path.extension)
            vars.addVariable("sequence", "${index + 1}".padStart(length, '0'))
            path.parent?.apply {
                vars.addVariable("parent", path.parent.name)
            }
            UniversalFileVariable(vars)
        }
    }

    override fun sharedPatternVariables(): PatternVariables {
        val vars = MapPatternVariables()
        vars.addVariable("sourceItemTitle", sourceItem.title)
        val date = sourceItem.date.toLocalDate()
        vars.addVariable("sourceItemDate", date.toString())
        vars.addVariable("sourceItemYear", date.year.toString())
        vars.addVariable("sourceItemMonth", date.month.toString())
        vars.addVariable("sourceItemDayOfMonth", date.dayOfMonth.toString())
        sourceItem.attributes.forEach { (t, u) ->
            vars.addVariable("attr.$t", u.toString())
        }
        return vars
    }
}

