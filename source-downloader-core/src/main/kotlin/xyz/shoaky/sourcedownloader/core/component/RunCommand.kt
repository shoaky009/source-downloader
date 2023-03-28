package xyz.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.util.StreamUtils
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.util.Jackson

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
            cmds.add(sourceContent.summarySubject())
        }
        val processBuilder = ProcessBuilder(*cmds.toTypedArray())
        return processBuilder.start()
    }

    fun run(sourceContent: SourceContent): Process {
        return process(sourceContent)
    }
}

object RunCommandSupplier : SdComponentSupplier<RunCommand> {
    override fun apply(props: ComponentProps): RunCommand {
        val command = props.properties["command"] ?: throw RuntimeException("command is null")
        val enableSummary = props.properties["withSubjectSummary"] as Boolean? ?: false
        if (command is List<*>) {
            return RunCommand(command.map { it.toString() }, enableSummary)
        }
        val convert = Jackson.convert(command, jacksonTypeRef<Map<String, String>>()).values.toList()
        return RunCommand(convert, enableSummary)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType("command", RunAfterCompletion::class)
        )
    }

    override fun getComponentClass(): Class<RunCommand> {
        return RunCommand::class.java
    }

}