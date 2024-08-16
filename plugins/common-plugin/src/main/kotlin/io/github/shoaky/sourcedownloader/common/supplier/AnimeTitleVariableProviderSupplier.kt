package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.AnimeTitleVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object AnimeTitleVariableProviderSupplier : ComponentSupplier<AnimeTitleVariableProvider> {

    override fun apply(
        context: CoreContext,
        props: Properties
    ): AnimeTitleVariableProvider {
        return AnimeTitleVariableProvider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("anime-title")
        )
    }

    override fun supportNoArgs(): Boolean {
        return true
    }
}