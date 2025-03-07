package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.KeywordIntegration
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import java.nio.file.Path

object KeywordIntegrationSupplier : ComponentSupplier<KeywordIntegration> {

    override fun apply(context: CoreContext, props: Properties): KeywordIntegration {
        val keywords = props.getOrDefault<List<String>>("keywords", emptyList())
        val keywordsFile = props.getOrNull<Path>("keywords-file")
        return KeywordIntegration(
            keywords,
            keywordsFile
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("keyword"),
            ComponentType.itemFilter("keyword"),
        )
    }
}