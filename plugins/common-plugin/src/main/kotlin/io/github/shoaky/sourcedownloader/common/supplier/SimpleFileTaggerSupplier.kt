package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.tagger.SimpleFileTagger
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

internal object SimpleFileTaggerSupplier : ComponentSupplier<SimpleFileTagger> {

    override fun apply(context: CoreContext, props: Properties): SimpleFileTagger {
        props.getOrNull<Map<String, String>>("external-mapping")?.let {
            return SimpleFileTagger(it)
        }
        return SimpleFileTagger()
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileTagger("simple")
        )
    }

    override fun supportNoArgs(): Boolean = true

}