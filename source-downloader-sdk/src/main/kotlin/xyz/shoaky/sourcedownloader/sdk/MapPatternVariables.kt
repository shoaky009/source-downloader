package xyz.shoaky.sourcedownloader.sdk

class MapPatternVariables() : PatternVariables {
    constructor(variables: Map<String, String>) : this() {
        this.variables.putAll(variables)
    }

    private val variables = mutableMapOf<String, String>()

    override fun getVariables(): Map<String, String> {
        return variables
    }
}