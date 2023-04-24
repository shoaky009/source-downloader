package xyz.shoaky.sourcedownloader.core.processor

import org.slf4j.LoggerFactory
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import java.nio.file.Path

class VariableProvidersAggregation(
    private val sourceItem: SourceItem,
    providers: List<VariableProvider>,
    private val strategy: VariableConflictStrategy = VariableConflictStrategy.SMART,
    private val variableNameMapping: Map<String, String> = emptyMap()
) : SourceItemGroup {

    private val groups = providers.associateBy({ it }, {
        NameMappingGroup(
            it.createSourceGroup(sourceItem),
            variableNameMapping
        )
    })

    private val strategyGroup: SourceItemGroup by lazy {
        when (strategy) {
            VariableConflictStrategy.ANY -> AnyItemGroup(groups)
            VariableConflictStrategy.VOTE -> VoteItemGroup(groups)
            VariableConflictStrategy.ACCURACY -> AccuracyItemGroup(groups)
            else -> SmartItemGroup(groups)
        }
    }

    override fun sharedPatternVariables(): PatternVariables {
        return strategyGroup.sharedPatternVariables()
    }

    override fun sourceFiles(paths: List<Path>): List<SourceFile> {
        return strategyGroup.sourceFiles(paths)
    }
}

class NameMappingPatternVariables(
    private val patternVariables: PatternVariables,
    private val nameMapping: Map<String, String>
) : PatternVariables {

    override fun variables(): Map<String, String> {
        return patternVariables.variables().mapKeys { nameMapping[it.key] ?: it.key }
    }
}

private class NameMappingGroup(
    private val sourceItemGroup: SourceItemGroup,
    private val nameMapping: Map<String, String>
) : SourceItemGroup {
    override fun sharedPatternVariables(): PatternVariables {
        return NameMappingPatternVariables(sourceItemGroup.sharedPatternVariables(), nameMapping)
    }

    override fun sourceFiles(paths: List<Path>): List<SourceFile> {
        return sourceItemGroup.sourceFiles(paths).map {
            NameMappingSourceFile(it, nameMapping)
        }
    }

    private class NameMappingSourceFile(
        private val sourceFile: SourceFile,
        private val nameMapping: Map<String, String>
    ) : SourceFile {
        override fun patternVariables(): PatternVariables {
            return NameMappingPatternVariables(sourceFile.patternVariables(), nameMapping)
        }
    }
}


private abstract class AggregationItemGroup(
    private val groups: Map<VariableProvider, SourceItemGroup>,
) : SourceItemGroup {
    override fun sourceFiles(paths: List<Path>): List<SourceFile> {
        val map = groups.mapValues {
            it.value.sourceFiles(paths)
        }
        val providerFiles = List(paths.size) { index ->
            map.getValuesAtIndex(index)
        }
        return mergeFiles(providerFiles)
    }

    override fun sharedPatternVariables(): PatternVariables {
        return aggrSharedPatternVariables(groups)
    }

    protected abstract fun aggrSharedPatternVariables(groups: Map<VariableProvider, SourceItemGroup>): PatternVariables

    protected abstract fun mergeFiles(
        providerFiles: List<List<Pair<VariableProvider, SourceFile>>>): List<SourceFile>

}

private class AnyItemGroup(
    groups: Map<VariableProvider, SourceItemGroup>,
) : AggregationItemGroup(groups) {
    override fun mergeFiles(providerFiles: List<List<Pair<VariableProvider, SourceFile>>>): List<SourceFile> {
        return List(providerFiles.size) { index ->
            val sourceFiles = providerFiles.map { it[index] }.map { it.second }
            sourceFiles.reduce(this::combine)
        }
    }

    override fun aggrSharedPatternVariables(groups: Map<VariableProvider, SourceItemGroup>): PatternVariables {
        val patternVariablesList = groups.values.map { it.sharedPatternVariables() }
        val result = mutableMapOf<String, String>()
        patternVariablesList.forEach {
            result.putAll(it.variables())
        }
        return MapPatternVariables(result)
    }

    private fun combine(sf1: SourceFile, sf2: SourceFile): SourceFile {
        return CombineSourceFile(sf1, sf2)
    }

    class CombineSourceFile(
        private val first: SourceFile,
        private val second: SourceFile
    ) : SourceFile {

        private val patternVariables = run {
            val var1 = first.patternVariables().variables()
            val var2 = second.patternVariables().variables()
            MapPatternVariables(var1 + var2)
        }

        override fun patternVariables(): PatternVariables {
            return patternVariables
        }
    }
}


private data class ValueCount(val value: String, val count: Int) : Comparable<ValueCount> {
    override fun compareTo(other: ValueCount): Int {
        return count.compareTo(other.count)
    }
}

private class VoteItemGroup(
    groups: Map<VariableProvider, SourceItemGroup>,
) : AggregationItemGroup(groups) {
    override fun aggrSharedPatternVariables(groups: Map<VariableProvider, SourceItemGroup>): PatternVariables {
        val variablesList = groups.values.map { it.sharedPatternVariables().variables() }
        val valueCounts = variablesList
            .flatMap { it.entries }
            .groupBy { it.key }
            .mapValues { entry ->
                entry.value
                    .groupingBy { it.value }
                    .eachCount()
                    .map { ValueCount(it.key, it.value) }
                    .sortedDescending().max()
            }.mapValues { it.value.value }
        return MapPatternVariables(valueCounts)
    }

    override fun mergeFiles(providerFiles: List<List<Pair<VariableProvider, SourceFile>>>): List<SourceFile> {
        return providerFiles.map { list ->
            val valueCounts = list.map {
                it.second.patternVariables().variables()
            }.flatMap { it.entries }
                .groupBy { it.key }
                .mapValues { entry ->
                    entry.value
                        .groupingBy { it.value }
                        .eachCount()
                        .map { ValueCount(it.key, it.value) }
                        .sortedDescending().max()
                }.mapValues { it.value.value }
            UniversalSourceFile(
                MapPatternVariables(valueCounts)
            )
        }
    }
}


private class AccuracyItemGroup(
    groups: Map<VariableProvider, SourceItemGroup>,
) : AggregationItemGroup(groups) {
    override fun aggrSharedPatternVariables(groups: Map<VariableProvider, SourceItemGroup>): PatternVariables {
        val variableMap = mutableMapOf<String, String>()
        groups.mapValues { it.value.sharedPatternVariables().variables() }
            .toList()
            .sortedBy { it.first.accuracy }
            .forEach {
                variableMap.putAll(it.second)
            }
        return MapPatternVariables(variableMap)
    }

    override fun mergeFiles(providerFiles: List<List<Pair<VariableProvider, SourceFile>>>): List<SourceFile> {
        return providerFiles.map { list ->
            val variableMap = mutableMapOf<String, String>()
            list.sortedBy { it.first.accuracy }
                .forEach {
                    variableMap.putAll(it.second.patternVariables().variables())
                }
            UniversalSourceFile(MapPatternVariables(variableMap))
        }
    }
}

private class SmartItemGroup(
    groups: Map<VariableProvider, SourceItemGroup>,
) : AggregationItemGroup(groups) {
    override fun aggrSharedPatternVariables(groups: Map<VariableProvider, SourceItemGroup>): PatternVariables {
        val variablesList = groups.entries.map { (provider, group) ->
            val variables = group.sharedPatternVariables().variables()
            provider to variables
        }
        val variableMap = variablesList
            .flatMap { pair -> pair.second.mapValues { ValueAccuracy(it.value, pair.first.accuracy) }.entries }
            .groupBy { it.key }
            .mapValues { entry ->
                entry.value
                    .groupingBy { it.value }
                    .eachCount()
                    .map { (value, count) ->
                        ValueAccuracyCount(value.value, value.accuracy, count)
                    }
            }
        if (log.isDebugEnabled) {
            log.debug("aggrSharedPatternVariables variableMap=$variableMap")
        }

        val result = variableMap.mapValues { it.value.sortedDescending().max() }
            .mapValues { it.value.value }
        return MapPatternVariables(result)
    }

    override fun mergeFiles(providerFiles: List<List<Pair<VariableProvider, SourceFile>>>): List<SourceFile> {
        return providerFiles.map { list ->
            val variableMap = list.map {
                val variables = it.second.patternVariables().variables()
                it.first to variables
            }.flatMap { pair -> pair.second.mapValues { ValueAccuracy(it.value, pair.first.accuracy) }.entries }
                .groupBy { it.key }
                .mapValues { entry ->
                    entry.value
                        .groupingBy { it.value }
                        .eachCount()
                        .map { (value, count) ->
                            ValueAccuracyCount(value.value, value.accuracy, count)
                        }
                }
            if (log.isDebugEnabled) {
                log.debug("mergeFiles: $variableMap")
            }
            val result = variableMap.mapValues { it.value.sortedDescending().max() }
                .mapValues { it.value.value }
            UniversalSourceFile(MapPatternVariables(result))
        }
    }

    private class ValueAccuracy(val value: String, val accuracy: Int) : Comparable<ValueAccuracy> {
        override fun compareTo(other: ValueAccuracy): Int {
            return accuracy.compareTo(other.accuracy)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ValueAccuracy
            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "ValueAccuracy(value='$value', accuracy=$accuracy)"
        }


    }

    private class ValueAccuracyCount(val value: String, val accuracy: Int, val count: Int) : Comparable<ValueAccuracyCount> {
        override fun compareTo(other: ValueAccuracyCount): Int {
            return accuracy.compareTo(other.accuracy) + count.compareTo(other.count)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ValueAccuracy
            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "ValueAccuracyCount(value='$value', accuracy=$accuracy, count=$count)"
        }

    }

    companion object {
        private val log = LoggerFactory.getLogger(SmartItemGroup::class.java)
    }
}

private fun <K, V> Map<K, List<V>>.getValuesAtIndex(index: Int): List<Pair<K, V>> {
    return this.map {
        it.key to it.value[index]
    }
}
