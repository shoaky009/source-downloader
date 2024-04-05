package io.github.shoaky.sourcedownloader.foreign.component

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.foreign.methods.VariableProviderMethods
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

open class ForeignVariableProvider(
    private val client: ForeignStateClient,
    private val paths: VariableProviderMethods
) : VariableProvider {

    private val remoteAccuracy: Int by lazy {
        val postState = client.postState(
            paths.accuracy,
            emptyMap<String, Any>(),
            jacksonTypeRef<JsonNode>()
        )
        postState.get("result").intValue()
    }

    override val accuracy: Int
        get() = remoteAccuracy

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        TODO()
    }

    override fun support(sourceItem: SourceItem): Boolean {
        val postState = client.postState(
            paths.accuracy,
            emptyMap<String, Any>(),
            jacksonTypeRef<JsonNode>()
        )
        return postState.get("result").booleanValue()
    }
}

