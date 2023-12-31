package io.github.shoaky.sourcedownloader.foreign

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.FileVariable
import io.github.shoaky.sourcedownloader.sdk.PatternVariables

class ForeignFileVariable : FileVariable {

    lateinit var client: ForeignStateClient
    lateinit var patternVariablesPath: String

    override fun patternVariables(): PatternVariables {
        return client.postState(
            patternVariablesPath,
            emptyMap<String, Any>(),
            jacksonTypeRef()
        )
    }

}