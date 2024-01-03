package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.HtmlFileResolver
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object HtmlFileResolverSupplier : ComponentSupplier<HtmlFileResolver> {

    override fun apply(context: CoreContext, props: Properties): HtmlFileResolver {
        return HtmlFileResolver(
            props.get("css-selector"),
            props.get("extract-attribute"),
            props.getOrDefault<Boolean>("direct-mode", false),
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.fileResolver("html"))
    }
}