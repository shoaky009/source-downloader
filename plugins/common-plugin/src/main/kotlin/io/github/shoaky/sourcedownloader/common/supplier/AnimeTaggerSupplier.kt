package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.AnimeTagger
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object AnimeTaggerSupplier : ComponentSupplier<AnimeTagger> {

    override fun apply(context: CoreContext, props: Properties): AnimeTagger {
        return AnimeTagger
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileTagger("anime")
        )
    }

    override fun autoCreateDefault(): Boolean {
        return true
    }
}