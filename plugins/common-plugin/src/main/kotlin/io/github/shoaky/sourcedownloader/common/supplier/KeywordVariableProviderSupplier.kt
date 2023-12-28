package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.KeywordVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import java.nio.file.Path

object KeywordVariableProviderSupplier : ComponentSupplier<KeywordVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): KeywordVariableProvider {
        val keywords = props.getOrDefault<List<String>>("keywords", emptyList())
        val keywordsFile = props.getOrNull<Path>("keywords-file")
        val prefixes = props.getOrDefault<List<Char>>("prefixes", listOf('(', '['))
        val suffixes = props.getOrDefault<List<Char>>("suffixes", listOf(')', ']'))
        return KeywordVariableProvider(
            keywords,
            keywordsFile,
            prefixes,
            suffixes
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("keyword")
        )
    }
}