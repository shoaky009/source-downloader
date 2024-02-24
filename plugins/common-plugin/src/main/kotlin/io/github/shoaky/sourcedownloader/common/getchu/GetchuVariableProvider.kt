package io.github.shoaky.sourcedownloader.common.getchu

import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

class GetchuVariableProvider(
    private val client: GetchuClient = GetchuClient
) : VariableProvider {

    override fun createItemGroup(sourceItem: SourceItem): SourceItemGroup {
        return GetchuItemGroup(sourceItem, client)
    }

    override fun support(sourceItem: SourceItem): Boolean {
        return true
    }

    companion object {

        val isbnRegex = Regex("[a-zA-Z]+-[a-zA-Z0-9]+")
    }
}

class GetchuItemGroup(
    private val sourceItem: SourceItem,
    private val client: GetchuClient,
) : SourceItemGroup {

    override fun sharedPatternVariables(): PatternVariables {
        val isbn = findIsbn(sourceItem)
        if (isbn != null) {
            return searchIsbn(isbn)
        }

        val searchResult = client.searchKeyword(sourceItem.title)
        if (searchResult.isEmpty()) {
            return PatternVariables.EMPTY
        }
        // 后续看情况优化
        return client.itemDetail(searchResult.first().url) ?: PatternVariables.EMPTY
    }

    private fun searchIsbn(isbn: String): PatternVariables {
        return client.searchKeyword(isbn)
            // isbn有多个一般是不同版本有些广告语，取标题最短的
            .minByOrNull {
                it.title.length
            }?.let {
                client.itemDetail(it.url)
            } ?: PatternVariables.EMPTY
    }

    override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
        return paths.map { FileVariable.EMPTY }
    }

    private fun findIsbn(sourceItem: SourceItem): String? {
        return GetchuVariableProvider.isbnRegex.find(sourceItem.title)?.value
    }


}