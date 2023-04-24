package xyz.shoaky.sourcedownloader.core.file

import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PatternVariables

class SharedPatternVariables(
    patternVariables: PatternVariables,
) : PatternVariables by patternVariables {

    private val sharedVariables = MapPatternVariables()
    private val patternVariables = patternVariables.variables()
    override fun variables(): Map<String, String> {
        return sharedVariables.variables() + patternVariables
    }

    fun addShared(patternVariables: PatternVariables) {
        patternVariables.variables().forEach(sharedVariables::addVariable)
    }
}