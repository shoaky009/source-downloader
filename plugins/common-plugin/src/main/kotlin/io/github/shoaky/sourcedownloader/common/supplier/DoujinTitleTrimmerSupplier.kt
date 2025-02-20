package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.dlsite.DoujinTitleTrimmer
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object DoujinTitleTrimmerSupplier : ComponentSupplier<DoujinTitleTrimmer> {

    override fun apply(context: CoreContext, props: Properties): DoujinTitleTrimmer {
        return DoujinTitleTrimmer
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.trimmer("doujin")
        )
    }
}