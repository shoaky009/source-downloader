package xyz.shoaky.sourcedownloader.component

import org.springframework.util.StreamUtils
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.RunAfterCompletion

class RunCommand(
    private val command: List<String>,
    private val withSubjectSummary: Boolean = false
) : RunAfterCompletion {

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
        val cmds = command.toMutableList()
        if (withSubjectSummary) {
            cmds.add(sourceContent.summaryContent())
        }
        val processBuilder = ProcessBuilder(*cmds.toTypedArray())
        return processBuilder.start()
    }

    fun run(sourceContent: SourceContent): Process {
        return process(sourceContent)
    }
}

