package io.github.shoaky.sourcedownloader.component.supplier

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import io.github.shoaky.sourcedownloader.component.*
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.*

object CompositeItemFileResolverSupplier : ComponentSupplier<CompositeItemFileResolver> {

    override fun apply(context: CoreContext, props: Properties): CompositeItemFileResolver {
        val selector = createSelector(
            context,
            props,
            ComponentTopType.ITEM_FILE_RESOLVER,
            object : TypeReference<ItemFileResolver>() {})
        return CompositeItemFileResolver(selector)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileResolver("composite")
        )
    }

}

object CompositeDownloaderSupplier : ComponentSupplier<CompositeDownloader> {

    override fun apply(context: CoreContext, props: Properties): CompositeDownloader {
        val selector = createSelector(
            context,
            props,
            ComponentTopType.DOWNLOADER,
            object : TypeReference<Downloader>() {})
        return CompositeDownloader(selector)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("composite")
        )
    }

}

private fun <T : SdComponent> createSelector(
    context: CoreContext,
    props: Properties,
    type: ComponentTopType,
    typeReference: TypeReference<T>
): ComponentSelector<T> {

    val default = props.get<String>("default").let {
        context.getComponent(
            type,
            it,
            typeReference
        )
    }

    val rules = props.get<JsonNode>("rules").map {
        val expression = ExpressionItemFilter.buildScript(it.get("expression").textValue())
        val component = context.getComponent(
            type,
            it.get("component").textValue(),
            typeReference
        )
        ComponentSelectRule(expression, component)
    }
    return ComponentSelector(default, rules)
}