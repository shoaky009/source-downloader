package io.github.shoaky.sourcedownloader.component.supplier

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import io.github.shoaky.sourcedownloader.component.ComponentSelectRule
import io.github.shoaky.sourcedownloader.component.ComponentSelector
import io.github.shoaky.sourcedownloader.component.CompositeDownloader
import io.github.shoaky.sourcedownloader.component.CompositeItemFileResolver
import io.github.shoaky.sourcedownloader.core.expression.CelCompiledExpressionFactory
import io.github.shoaky.sourcedownloader.core.expression.CompiledExpressionFactory
import io.github.shoaky.sourcedownloader.core.expression.sourceItemDefs
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.*

object CompositeItemFileResolverSupplier : ComponentSupplier<CompositeItemFileResolver> {

    override fun apply(context: CoreContext, props: Properties): CompositeItemFileResolver {
        val selector = createSelector(
            context,
            props,
            ComponentRootType.ITEM_FILE_RESOLVER,
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
            ComponentRootType.DOWNLOADER,
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
    type: ComponentRootType,
    typeReference: TypeReference<T>,
    expressionFactory: CompiledExpressionFactory = CelCompiledExpressionFactory
): ComponentSelector<T> {

    val default = props.get<String>("default").let {
        context.getComponent(
            type,
            it,
            typeReference
        )
    }

    val rules = props.get<JsonNode>("rules").map {
        val raw = it.get("expression").textValue()
        // 这里不一定是SourceItem的
        val expression = expressionFactory.create(raw, Boolean::class.java, sourceItemDefs())
        val component = context.getComponent(
            type,
            it.get("component").textValue(),
            typeReference
        )
        ComponentSelectRule(expression, component)
    }
    return ComponentSelector(default, rules)
}