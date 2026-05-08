package io.github.shoaky.sourcedownloader.component.supplier

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.component.RunCommand
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema
import io.github.shoaky.sourcedownloader.sdk.util.Jackson

object RunCommandSupplier : ComponentSupplier<RunCommand> {

    override fun apply(context: CoreContext, props: Properties): RunCommand {
        val command = props.getRaw("command")
        val enableSummary = props.getOrDefault("withSubjectSummary", false)
        if (command is List<*>) {
            return RunCommand(command.map { it.toString() }, enableSummary)
        }
        val convert = Jackson.convert(command, jacksonTypeRef<Map<String, String>>()).values.toList()
        return RunCommand(convert, enableSummary)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.listener("command")
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("command"),
                properties = mapOf(
                    "command" to JsonSchema(
                        description = "命令参数列表或有序对象值列表",
                        oneOf = listOf(
                            JsonSchema(
                                type = "array",
                                items = JsonSchema(type = "string")
                            ),
                            JsonSchema(
                                type = "object",
                                additionalProperties = JsonSchema(type = "string")
                            )
                        )
                    ),
                    "withSubjectSummary" to JsonSchema(
                        type = "boolean",
                        default = false
                    )
                )
            )
        )
    }

}
