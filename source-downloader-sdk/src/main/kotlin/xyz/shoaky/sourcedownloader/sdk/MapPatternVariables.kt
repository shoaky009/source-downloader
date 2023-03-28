package xyz.shoaky.sourcedownloader.sdk

class MapPatternVariables() : PatternVariables {
    constructor(variables: Map<String, String>) : this() {
        this.vars.putAll(variables)
    }

    private val vars: MutableMap<String, String> = mutableMapOf()

    override fun getVariables(): Map<String, String> {
        return vars
    }

    fun addVariable(name: String, value: String) {
        vars[name] = value
    }

    override fun toString(): String {
        return vars.toString()
    }
}