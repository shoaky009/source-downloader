package xyz.shoaky.sourcedownloader.core

import com.fasterxml.jackson.annotation.JsonInclude
import xyz.shoaky.sourcedownloader.sdk.component.Components


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class ComponentProp(
    val explanation: String = "",
    val example: String = "",
    val default: String = ""
)

@Retention(AnnotationRetention.RUNTIME)
annotation class VariableDescription(
    val name: String,
    val explanation: String = "",
    val example: String = "",
    val reliability: String = "",
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ComponentDescription(
    val name: String,
    val types: List<TypeDescription>,
    val props: List<ComponentProp>,
    val rules: List<RuleDescription> = emptyList(),
    val variableDescriptions: List<VariableDescription>? = null
)

data class TypeDescription(
    val topType: Components,
    val subType: List<String>,
    val description: String,
)

// 只允许Source为SystemFileSource？
data class RuleDescription(
    //"只允许" or "不允许"
    val isAllow: String,
    val type: String,
    val value: String,
)
