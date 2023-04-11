package xyz.shoaky.sourcedownloader.core.file

import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PatternVariables

class SharedPatternVariables(
    private val patternVariables: PatternVariables,
) : PatternVariables by patternVariables {

    private val sharedVariables = MapPatternVariables()
    override fun variables(): Map<String, String> {
        return patternVariables.variables() + sharedVariables.variables()
    }

    fun addVariables(patternVariables: PatternVariables) {
        patternVariables.variables().forEach(sharedVariables::addVariable)
    }
}