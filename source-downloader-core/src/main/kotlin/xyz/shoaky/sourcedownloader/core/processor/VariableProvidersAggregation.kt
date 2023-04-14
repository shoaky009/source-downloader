package xyz.shoaky.sourcedownloader.core.processor

import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import java.nio.file.Path

class VariableProvidersAggregation(
    private val providers: List<VariableProvider>
) {
    fun aggrVariables(sourceItem: SourceItem): SourceItemGroup {
        val associateBy = providers.associateBy({
            it
        }, { it.createSourceGroup(sourceItem) })
        return SourceItemGroupAggr(associateBy)
    }
}

private class SourceItemGroupAggr(
    private val groups: Map<VariableProvider, SourceItemGroup>
) : SourceItemGroup {
    override fun sourceFiles(paths: List<Path>): List<SourceFile> {
        val res = mutableListOf<List<SourceFile>>()
        for (group in groups) {
            val sourceFiles = group.value.sourceFiles(paths)
            res.add(sourceFiles)
        }
        return List(paths.size) { index ->
            val sourceFiles = res.map { it[index] }
            sourceFiles.reduce(this::combine)
        }
    }

    override fun sharedPatternVariables(): PatternVariables {
        val map = groups.values.map { it.sharedPatternVariables() }
        val mapPatternVariables = MapPatternVariables()
        map.forEach {
            mapPatternVariables.addVariables(it)
        }
        return mapPatternVariables
    }

    private fun combine(sf1: SourceFile, sf2: SourceFile): SourceFile {
        return CombineSourceFile(sf1, sf2)
    }

    class CombineSourceFile(
        private val first: SourceFile,
        private val second: SourceFile
    ) : SourceFile {

        private val patternVariables = run {
            val pv1 = first.patternVariables()
            val pv2 = second.patternVariables()
            val var1 = pv1.variables()
            val var2 = pv2.variables()
            // TODO 处理冲突的情况，优先级
            val allVariables = var1 + var2
            MapPatternVariables(allVariables)
        }

        override fun patternVariables(): PatternVariables {
            return patternVariables
        }
    }
}