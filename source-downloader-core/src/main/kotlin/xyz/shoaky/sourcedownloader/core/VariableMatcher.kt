package xyz.shoaky.sourcedownloader.core

interface VariableMatcher {

    fun match(key: String, value: String): Boolean

}

class RegexVariableMatcher(
    private val regex: Regex
) : VariableMatcher {

    override fun match(key: String, value: String): Boolean {
        return regex.matches(value)
    }

    override fun toString(): String {
        return this.regex.toString()
    }
}