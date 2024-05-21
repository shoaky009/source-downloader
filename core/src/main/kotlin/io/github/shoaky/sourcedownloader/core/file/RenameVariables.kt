package io.github.shoaky.sourcedownloader.core.file

data class RenameVariables(
    val variables: Map<String, Any>,
    val processedVariables: Map<String, String> = emptyMap(),
    /**
     * 由VariableProvider提供的变量，已经包含在[variables]中
     */
    val patternVariables: Map<String, String> = emptyMap()
) {

    val allVariables: Map<String, Any> by lazy {
        variables + processedVariables
    }

    companion object {

        val EMPTY = RenameVariables(emptyMap())
    }
}