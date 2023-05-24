package xyz.shoaky.sourcedownloader.core

interface VariableMatcher {

    fun match(value: String): Boolean

}

class RegexVariableMatcher(
    private val regex: Regex
) : VariableMatcher {

    override fun match(value: String): Boolean {
        return regex.matches(value)
    }
}