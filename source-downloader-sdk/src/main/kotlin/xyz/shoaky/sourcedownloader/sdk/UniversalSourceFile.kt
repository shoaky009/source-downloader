package xyz.shoaky.sourcedownloader.sdk

class UniversalSourceFile(
    private val patternVariables: PatternVariables
) : SourceFile {
    override fun patternVariables(): PatternVariables {
        return patternVariables
    }

    override fun toString(): String {
        return "UniversalSourceFile(patternVariables=$patternVariables)"
    }

}