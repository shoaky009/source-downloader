package xyz.shoaky.sourcedownloader.component

import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import java.util.function.Predicate

class RegexSourceItemFilter(
    private val regexes: List<Regex>
) : SourceItemFilter {

    private val predicate: Predicate<SourceItem> =
        Predicate {
            this.regexes.any { regex ->
                it.title.contains(regex)
            }.not()
        }

    override fun test(item: SourceItem): Boolean {
        val test = predicate.test(item)
        if (test.not()) {
            log.debug("Regex Filtered: {}", item)
        }
        return test
    }
}