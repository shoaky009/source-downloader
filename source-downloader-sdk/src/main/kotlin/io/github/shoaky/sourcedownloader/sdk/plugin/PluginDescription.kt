package io.github.shoaky.sourcedownloader.sdk.plugin

data class PluginDescription(
    val name: String,
    val version: String
) {
    fun fullName(): String = "$name:$version"
}