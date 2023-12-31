package io.github.shoaky.sourcedownloader.foreign.methods

data class VariableProviderMethods(
    val createItemGroup: String = "/variable_provider/create_item_group",
    val support: String = "/variable_provider/support",
    val accuracy: String = "/variable_provider/accuracy",
    val filePatternVariables: String = "/variable_provider/file_pattern_variables",
    val sharedPatternVariables: String = "/variable_provider/shared_pattern_variables",
)
