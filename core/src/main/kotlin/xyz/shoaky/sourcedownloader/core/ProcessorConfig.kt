package xyz.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonValue
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.component.*
import java.nio.file.Path
import java.time.Duration
import kotlin.reflect.KClass

data class ProcessorConfig(
    val name: String,
    val trigger: ComponentId,
    val source: ComponentId,
    val creator: ComponentId,
    val downloader: ComponentId = ComponentId("downloader:http"),
    val mover: ComponentId = ComponentId("mover:move"),
    val savePath: Path,
    val fileMode: RenameMode = RenameMode.MOVE,
    val options: Options = Options(),
) {

    fun getSourceInstanceName(): String {
        return source.getInstanceName(Source::class)
    }

    fun getCreatorInstanceName(): String {
        return creator.getInstanceName(SourceContentCreator::class)
    }

    fun getDownloaderInstanceName(): String {
        return downloader.getInstanceName(Downloader::class)
    }

    fun getMoverInstanceName(): String {
        return mover.getInstanceName(FileMover::class)
    }

    fun getTriggerInstanceName(): String {
        return trigger.getInstanceName(Trigger::class)
    }

    data class Options(
        val blacklistRegex: List<Regex> = emptyList(),
        val fileSavePathPattern: PathPattern? = null,
        val filenamePattern: PathPattern? = null,
        val runAfterCompletion: List<ComponentId> = emptyList(),
        val renameTaskInterval: Duration = Duration.ofMinutes(5),
        val downloadCategory: String? = null
    )

    data class ComponentId(
        @JsonValue
        val id: String,
    ) {
        fun <T : SdComponent> getInstanceName(klass: KClass<T>): String {
            val split = id.split(":")
            if (split.isEmpty()) {
                throw RuntimeException("重命名器配置错误:${id}")
            }
            return ComponentType(split.first(), klass).instanceName(split.last())
        }
    }
}


