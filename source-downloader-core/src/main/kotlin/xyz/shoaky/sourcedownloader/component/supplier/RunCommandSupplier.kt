package xyz.shoaky.sourcedownloader.component.supplier

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import xyz.shoaky.sourcedownloader.component.RunCommand
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.util.Jackson

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