package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.RegexTrimmer
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object RegexTrimmerSupplier : ComponentSupplier<RegexTrimmer> {

    override fun apply(context: CoreContext, props: Properties): RegexTrimmer {
        return RegexTrimmer(Regex(props.get<String>("regex")))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.trimmer("regex")
        )
    }
}