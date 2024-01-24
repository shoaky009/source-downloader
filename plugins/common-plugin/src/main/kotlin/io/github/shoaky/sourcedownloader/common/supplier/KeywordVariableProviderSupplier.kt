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
        return KeywordVariableProvider(
            keywords,
            keywordsFile,

            )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("keyword")
        )
    }
}