package io.github.shoaky.sourcedownloader.core.file

data class RenameVariables(
    val variables: Map<String, Any>,
    val processedVariables: Map<String, String> = emptyMap()
) {

    val allVariables: Map<String, Any> by lazy {
        variables + processedVariables
    }

    companion object {

        val EMPTY = RenameVariables(emptyMap(), emptyMap())
    }
}