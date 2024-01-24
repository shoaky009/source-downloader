package io.github.shoaky.sourcedownloader.sdk

class UniversalFileVariable(
    private val patternVariables: PatternVariables
) : FileVariable {
    override fun patternVariables(): PatternVariables {
        return patternVariables
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "UniversalSourceFile(patternVariables=$patternVariables)"
    }

    companion object {
        fun fromMap(variables: Map<String, String>): UniversalFileVariable {
            return UniversalFileVariable(MapPatternVariables(variables))
        }
    }
}