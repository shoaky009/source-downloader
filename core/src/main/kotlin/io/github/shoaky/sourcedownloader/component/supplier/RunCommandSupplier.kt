package io.github.shoaky.sourcedownloader.component.supplier

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.component.RunCommand
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import io.github.shoaky.sourcedownloader.sdk.util.Jackson

object RunCommandSupplier : ComponentSupplier<RunCommand> {

    override fun apply(props: Properties): RunCommand {
        val command = props.rawValues["command"] ?: throw RuntimeException("command is null")
        val enableSummary = props.rawValues["withSubjectSummary"] as Boolean? ?: false
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

}