package xyz.shoaky.sourcedownloader.sdk

class MapPatternVariables() : PatternVariables {
    constructor(variables: Map<String, String>) : this() {
        this.variables.putAll(variables)
    }

    private val variables: MutableMap<String, String> = mutableMapOf()

    override fun variables(): Map<String, String> {
        return variables
    }

    fun addVariable(name: String, value: String) {
        variables[name] = value
    }

    fun getVariables(): Map<String, String> {
        return variables
    }

    override fun toString(): String {
        return variables.toString()
    }
}