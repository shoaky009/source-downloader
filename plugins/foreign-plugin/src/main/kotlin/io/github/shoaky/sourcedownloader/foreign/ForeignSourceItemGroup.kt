package io.github.shoaky.sourcedownloader.foreign

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.foreign.methods.VariableProviderMethods
import io.github.shoaky.sourcedownloader.sdk.FileVariable
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItemGroup

class ForeignSourceItemGroup : SourceItemGroup {

    lateinit var client: ForeignStateClient
    lateinit var paths: VariableProviderMethods

    override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
        return client.postState(
            this.paths.filePatternVariables,
            mapOf("paths" to paths),
            jacksonTypeRef<List<ForeignFileVariable>>()
        ).onEach {
            it.client = this.client
            it.patternVariablesPath = TODO()
        }
    }

    override fun sharedPatternVariables(): PatternVariables {
        return client.postState(
            this.paths.sharedPatternVariables,
            emptyMap<String, Any>(),
            jacksonTypeRef()
        )
    }
}