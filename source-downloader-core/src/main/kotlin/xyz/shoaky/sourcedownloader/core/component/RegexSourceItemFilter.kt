package xyz.shoaky.sourcedownloader.core.component

import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.SourceFilter
import java.util.function.Predicate

class RegexSourceItemFilter(private val regexes: List<Regex>) : SourceFilter {

    private val predicate: Predicate<SourceItem> =
        Predicate {
            this.regexes.any { regex ->
                it.title.lowercase().contains(regex)
            }.not()
        }

    override fun test(item: SourceItem): Boolean {
        return predicate.test(item)
    }

}

object RegexSourceItemFilterSupplier : SdComponentSupplier<RegexSourceItemFilter> {

    override fun apply(props: ComponentProps): RegexSourceItemFilter {
        return RegexSourceItemFilter(props.parse())
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType("regex", SourceFilter::class))
    }

}