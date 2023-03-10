package xyz.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.core.type.TypeReference
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.SourceFilter
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.util.function.Predicate

class RegexSourceItemFilter(private val regexes: List<Regex>) : SourceFilter {

    private val predicate: Predicate<SourceItem> =
        Predicate {
            this.regexes.any { regex ->
                it.title.contains(regex)
            }.not()
        }

    override fun test(item: SourceItem): Boolean {
        val test = predicate.test(item)
        if (test.not()) {
            log.debug("Regex Filtered: $item")
        }
        return test
    }

}

object RegexSourceItemFilterSupplier : SdComponentSupplier<RegexSourceItemFilter> {

    override fun apply(props: ComponentProps): RegexSourceItemFilter {
        val regexes = props.properties["regexes"] ?: listOf<String>()
        val convert = Jackson.convert(regexes, object : TypeReference<List<Regex>>() {})
        return RegexSourceItemFilter(convert)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType("regex", SourceFilter::class))
    }

}