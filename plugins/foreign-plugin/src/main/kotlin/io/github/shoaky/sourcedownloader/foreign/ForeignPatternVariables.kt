package io.github.shoaky.sourcedownloader.foreign

import io.github.shoaky.sourcedownloader.foreign.methods.VariableProviderMethods
import io.github.shoaky.sourcedownloader.sdk.PatternVariables

class ForeignPatternVariables : PatternVariables {

    lateinit var client: ForeignStateClient
    lateinit var paths: VariableProviderMethods

    override fun variables(): Map<String, String> {
        TODO()
    }
}