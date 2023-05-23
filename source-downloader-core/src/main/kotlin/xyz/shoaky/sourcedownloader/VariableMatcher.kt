package xyz.shoaky.sourcedownloader

import com.fasterxml.jackson.annotation.JsonCreator

interface VariableMatcher {

    fun match(value: String): Boolean

}

class RegexVariableMatcher(
    private val regex: Regex
) : VariableMatcher {

    override fun match(value: String): Boolean {
        return regex.matches(value)
    }

    companion object {

        @JsonCreator
        fun create(value: String): RegexVariableMatcher {
            return RegexVariableMatcher(value.toRegex())
        }
    }
}