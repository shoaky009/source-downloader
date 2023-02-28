package xyz.shoaky.sourcedownloader.core.component

import org.springframework.util.StreamUtils
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import java.nio.file.Path

class RunScript(private val scriptPath: Path) : RunAfterCompletion {

    private fun scriptCommand() = if (System.getProperty("os.name").contains("windows", true))
        "powershell.exe" else "/bin/sh"

    override fun accept(sourceContent: SourceContent) {
        val process = run(sourceContent)
        if (process.waitFor() != 0) {
            val result = StreamUtils.copyToString(process.inputStream, Charsets.UTF_8)
            log.warn("mikan completed task script exit code is not 0, result:$result")
        }
        if (log.isDebugEnabled) {
            val result = StreamUtils.copyToString(process.inputStream, Charsets.UTF_8)
            log.debug("script result is:$result")
        }
    }

    private fun process(sourceContent: SourceContent): Process {
        //TODO key相同合并成范围值
        val values = sourceContent.attributes().values.flatten()
        val processBuilder = ProcessBuilder(scriptCommand(), scriptPath.toString(), *values.toTypedArray())
        return processBuilder.start()
    }

    fun run(sourceContent: SourceContent): Process {
        return process(sourceContent)
    }
}

object RunScriptSupplier : ComponentSupplier<RunScript> {
    override fun apply(props: ComponentProps): RunScript {
        val path = props.properties["path"]?.toString() ?: throw RuntimeException("path不能为空")
        return RunScript(Path.of(path))
    }

    override fun availableTypes(): List<ComponentType> {
        return listOf(
            ComponentType("script", RunAfterCompletion::class)
        )
    }

}