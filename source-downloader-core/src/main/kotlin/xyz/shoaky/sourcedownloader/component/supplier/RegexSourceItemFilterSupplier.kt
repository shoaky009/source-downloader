package xyz.shoaky.sourcedownloader.component.supplier

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import xyz.shoaky.sourcedownloader.component.RegexSourceItemFilter
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import xyz.shoaky.sourcedownloader.sdk.util.Jackson

object RegexSourceItemFilterSupplier : SdComponentSupplier<RegexSourceItemFilter> {

    override fun apply(props: ComponentProps): RegexSourceItemFilter {
        val regexes = props.getOrDefault("regexes", listOf<String>())
        val convert: List<Regex> = Jackson.convert(regexes, jacksonTypeRef())
        return RegexSourceItemFilter(convert)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType("regex", SourceItemFilter::class))
    }

    override fun getComponentClass(): Class<RegexSourceItemFilter> {
        return RegexSourceItemFilter::class.java
    }

    fun regexes(regexes: List<String> = emptyList()): RegexSourceItemFilter {
        return RegexSourceItemFilter(regexes.map { Regex(it) })
    }

}