package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import org.springframework.util.StreamUtils

/**
 * 执行命令
 */
class RunCommand(
    private val command: List<String>,
    private val withSubjectSummary: Boolean = false
) : RunAfterCompletion {

    override fun accept(itemContent: ItemContent) {
        val process = run(itemContent)
        if (process.waitFor() != 0) {
            val result = StreamUtils.copyToString(process.inputStream, Charsets.UTF_8)
            log.warn("mikan completed task script exit code is not 0, result:$result")
        }
        if (log.isDebugEnabled) {
            val result = StreamUtils.copyToString(process.inputStream, Charsets.UTF_8)
            log.debug("script result is:$result")
        }
    }

    private fun process(itemContent: ItemContent): Process {
        val cmds = command.toMutableList()
        if (withSubjectSummary) {
            cmds.add(itemContent.summaryContent())
        }

        if (log.isDebugEnabled) {
            log.debug("run command: ${cmds.joinToString(" ")}")
        }
        val processBuilder = ProcessBuilder(*cmds.toTypedArray())
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)
        return processBuilder.start()
    }

    fun run(itemContent: ItemContent): Process {
        return process(itemContent)
    }
}

