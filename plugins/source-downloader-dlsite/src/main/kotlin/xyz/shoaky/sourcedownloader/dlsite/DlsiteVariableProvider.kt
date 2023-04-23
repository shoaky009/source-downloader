package xyz.shoaky.sourcedownloader.dlsite

import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import xyz.shoaky.sourcedownloader.sdk.util.find

internal class DlsiteVariableProvider : VariableProvider {
    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        val dlsiteId = getDLsiteId(sourceItem) ?: throw IllegalArgumentException("not dlsite item")

        // val document = Jsoup.newSession().cookie()
        //     .url("")
        //     .get()

        val workInfo = DLsiteWorkInfo(
            dlsiteId,
            "test",
        )
        return FunctionalItemGroup {
            UniversalSourceFile(workInfo)
        }
    }

    override fun support(item: SourceItem): Boolean {
        return getDLsiteId(item) != null
    }

    companion object {

        private val idRegexes = arrayOf(
            Regex("RJ\\d+"),
            Regex("BJ\\d+"),
            Regex("VJ\\d+"),
            Regex("RT\\d+"),
            Regex("RE\\d+"),
        )

        private fun getDLsiteId(item: SourceItem): String? {
            val link = item.link.toString()
            return link.find(*idRegexes)
        }
    }
}

private data class DLsiteWorkInfo(
    val dlsiteId: String,
    val title: String,
    // more....
) : PatternVariables