package io.github.shoaky.sourcedownloader.integration.support

import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import org.springframework.stereotype.Component

@Component
object Item2ReplaceDeciderSupplier : ComponentSupplier<Item2ReplaceDecider> {

    override fun apply(context: CoreContext, props: Properties): Item2ReplaceDecider {
        return Item2ReplaceDecider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileReplacementDecider("idk-replace-decider")
        )
    }

    override fun supportNoArgs(): Boolean {
        return true
    }
}