package xyz.shoaky.sourcedownloader.core.component

import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.Source
import java.nio.file.Path
import kotlin.io.path.Path

class WatchFileSource(private val watchPath: Path) : Source {
    override fun fetch(): List<SourceItem> {
        TODO("Not yet implemented")
    }
}

object WatchFileSourceSupplier : ComponentSupplier<WatchFileSource> {
    override fun apply(props: ComponentProps): WatchFileSource {
        val path = props.properties["watch-path"]?.let { Path(it.toString()) }
            ?: throw RuntimeException("watch-path not null")
        return WatchFileSource(path)
    }

    override fun availableTypes(): List<ComponentType> {
        return listOf(
            ComponentType("watch", Source::class)
        )
    }

}