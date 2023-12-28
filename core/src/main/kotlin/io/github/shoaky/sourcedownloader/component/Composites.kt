package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Downloader
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import io.github.shoaky.sourcedownloader.sdk.component.SdComponent
import org.projectnessie.cel.tools.Script
import java.nio.file.Path

class CompositeDownloader(
    private val selector: ComponentSelector<Downloader>
) : Downloader {

    init {
        val notSameDownloadPath = buildList {
            this.add(selector.default)
            this.addAll(selector.rules.map { it.component })
        }.map { it.defaultDownloadPath() }
            .distinct().size != 1
        if (notSameDownloadPath) {
            throw IllegalArgumentException("Downloaders must have the same download path")
        }
    }

    override fun submit(task: DownloadTask): Boolean {
        return selector.select(task.sourceItem).submit(task)
    }

    override fun defaultDownloadPath(): Path {
        return selector.default.defaultDownloadPath()
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        selector.select(sourceItem).cancel(sourceItem, files)
    }

}

class CompositeItemFileResolver(
    private val selector: ComponentSelector<ItemFileResolver>
) : ItemFileResolver {

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val component = selector.select(sourceItem)
        return component.resolveFiles(sourceItem)
    }
}

class ComponentSelector<T : SdComponent>(
    val default: T,
    val rules: List<ComponentSelectRule<T>>
) {

    fun select(sourceItem: SourceItem): T {
        val variables = bindItemScriptVars(sourceItem)
        return rules.firstOrNull {
            it.script.execute(Boolean::class.java, variables) == true

        }?.component ?: default
    }
}

data class ComponentSelectRule<T : SdComponent>(
    val script: Script,
    val component: T
)