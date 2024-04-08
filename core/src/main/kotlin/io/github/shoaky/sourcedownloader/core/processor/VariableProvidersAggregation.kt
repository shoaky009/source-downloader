package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import org.slf4j.LoggerFactory

class VariableProvidersAggregation(
    private val sourceItem: SourceItem,
    providers: List<VariableProvider>,
    private val strategy: VariableConflictStrategy = VariableConflictStrategy.SMART,
    private val variableNameReplace: Map<String, String> = emptyMap()
) : VariableProvider {

    private val groups = providers.associateBy({ it }, {
        NameReplacePatternVariables(
            it.itemVariables(sourceItem),
            variableNameReplace
        )
    })

    private val strategyGroup by lazy {
        when (strategy) {
            VariableConflictStrategy.ANY -> AnyItemGroup(groups, variableNameReplace)
            VariableConflictStrategy.VOTE -> VoteItemGroup(groups, variableNameReplace)
            VariableConflictStrategy.ACCURACY -> AccuracyItemGroup(groups, variableNameReplace)
            else -> SmartItemGroup(groups, variableNameReplace)
        }
    }

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        return strategyGroup.itemVariables(sourceItem)

    }

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>,
    ): List<PatternVariables> {
        return strategyGroup.fileVariables(sourceItem, itemVariables, sourceFiles)
    }

    override val accuracy: Int
        get() = strategyGroup.accuracy

}

class NameReplacePatternVariables(
    private val patternVariables: PatternVariables,
    private val nameReplace: Map<String, String>
) : PatternVariables {

    override fun variables(): Map<String, String> {
        return patternVariables.variables().mapKeys { nameReplace[it.key] ?: it.key }
    }
}

private abstract class AggregationItemGroup(
    private val groups: Map<VariableProvider, PatternVariables>,
    private val variableNameReplace: Map<String, String>,
) : VariableProvider {

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        return aggrSharedPatternVariables(groups)
    }

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>,
    ): List<PatternVariables> {
        val map = groups.mapValues { (p, v) ->
            p.fileVariables(sourceItem, v, sourceFiles).map {
                NameReplacePatternVariables(it, variableNameReplace)
            }
        }
        val providerFiles = List(sourceFiles.size) { index ->
            map.getValuesAtIndex(index, PatternVariables.EMPTY)
        }
        return mergeFiles(providerFiles)

    }

    protected abstract fun aggrSharedPatternVariables(groups: Map<VariableProvider, PatternVariables>): PatternVariables

    protected abstract fun mergeFiles(
        providerFiles: List<List<Pair<VariableProvider, PatternVariables>>>
    ): List<PatternVariables>

}

private class AnyItemGroup(
    groups: Map<VariableProvider, PatternVariables>,
    variableNameReplace: Map<String, String>,
) : AggregationItemGroup(groups, variableNameReplace) {

    override fun mergeFiles(providerFiles: List<List<Pair<VariableProvider, PatternVariables>>>): List<PatternVariables> {
        return List(providerFiles.size) { index ->
            val sourceFiles = providerFiles.map { it[index] }.map { it.second }
            sourceFiles.reduce(this::combine)
        }
    }

    override fun aggrSharedPatternVariables(groups: Map<VariableProvider, PatternVariables>): PatternVariables {
        val patternVariablesList = groups.values.map { it }
        val result = mutableMapOf<String, String>()
        patternVariablesList.forEach {
            result.putAll(it.variables())
        }
        return MapPatternVariables(result)
    }

    private fun combine(sf1: PatternVariables, sf2: PatternVariables): PatternVariables {
        return CombineFileVariable(sf1, sf2)
    }

    class CombineFileVariable(
        private val first: PatternVariables,
        private val second: PatternVariables
    ) : PatternVariables {

        private val patternVariables = run {
            val var1 = first.variables()
            val var2 = second.variables()
            MapPatternVariables(var1 + var2)
        }

    }
}

private data class ValueCount(val value: String, val count: Int) : Comparable<ValueCount> {

    override fun compareTo(other: ValueCount): Int {
        return count.compareTo(other.count)
    }
}

private class VoteItemGroup(
    groups: Map<VariableProvider, PatternVariables>,
    variableNameReplace: Map<String, String>,
) : AggregationItemGroup(groups, variableNameReplace) {

    override fun aggrSharedPatternVariables(groups: Map<VariableProvider, PatternVariables>): PatternVariables {
        val variablesList = groups.values.map { it.variables() }
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

    override fun mergeFiles(providerFiles: List<List<Pair<VariableProvider, PatternVariables>>>): List<PatternVariables> {
        return providerFiles.map { list ->
            val valueCounts = list.map {
                it.second.variables()
            }.flatMap { it.entries }
                .groupBy { it.key }
                .mapValues { entry ->
                    entry.value
                        .groupingBy { it.value }
                        .eachCount()
                        .map { ValueCount(it.key, it.value) }
                        .sortedDescending().max()
                }.mapValues { it.value.value }
            MapPatternVariables(valueCounts)
        }
    }
}

private class AccuracyItemGroup(
    groups: Map<VariableProvider, PatternVariables>,
    variableNameReplace: Map<String, String>,
) : AggregationItemGroup(groups, variableNameReplace) {

    override fun aggrSharedPatternVariables(groups: Map<VariableProvider, PatternVariables>): PatternVariables {
        val variableMap = mutableMapOf<String, String>()
        groups.mapValues { it.value.variables() }
            .toList()
            .sortedBy { it.first.accuracy }
            .forEach {
                variableMap.putAll(it.second)
            }
        return MapPatternVariables(variableMap)
    }

    override fun mergeFiles(providerFiles: List<List<Pair<VariableProvider, PatternVariables>>>): List<PatternVariables> {
        return providerFiles.map { list ->
            val variableMap = mutableMapOf<String, String>()
            list.sortedBy { it.first.accuracy }
                .forEach {
                    variableMap.putAll(it.second.variables())
                }
            MapPatternVariables(variableMap)
        }
    }
}

private class SmartItemGroup(
    groups: Map<VariableProvider, PatternVariables>,
    variableNameReplace: Map<String, String>,
) : AggregationItemGroup(groups, variableNameReplace) {

    override fun aggrSharedPatternVariables(groups: Map<VariableProvider, PatternVariables>): PatternVariables {
        val variablesList = groups.entries.map { (provider, group) ->
            val variables = group.variables()
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

    override fun mergeFiles(providerFiles: List<List<Pair<VariableProvider, PatternVariables>>>): List<PatternVariables> {
        return providerFiles.map { list ->
            val variableMap = list.map {
                val variables = it.second.variables()
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
            MapPatternVariables(result)
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

    private class ValueAccuracyCount(val value: String, val accuracy: Int, val count: Int) :
        Comparable<ValueAccuracyCount> {

        override fun compareTo(other: ValueAccuracyCount): Int {
            val accuracyCmp = accuracy.compareTo(other.accuracy)
            return if (accuracyCmp != 0) {
                accuracyCmp
            } else {
                count.compareTo(other.count)
            }
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

private fun <K, V> Map<K, List<V>>.getValuesAtIndex(index: Int, default: V): List<Pair<K, V>> {
    return this.map {
        it.key to it.value.getOrElse(index) { _ -> default }
    }
}
