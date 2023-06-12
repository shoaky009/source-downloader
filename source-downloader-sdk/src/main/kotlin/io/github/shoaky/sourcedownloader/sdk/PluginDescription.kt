package io.github.shoaky.sourcedownloader.sdk

data class PluginDescription(
    val name: String,
    val version: String
) {
    fun fullName(): String = "$name:$version"
}