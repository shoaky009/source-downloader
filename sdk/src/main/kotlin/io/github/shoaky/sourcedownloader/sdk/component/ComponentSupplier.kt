package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties

interface ComponentSupplier<R : SdComponent> {

    /**
     * @param props component properties from storage
     * @throws ComponentException if props is invalid
     */
    fun apply(context: CoreContext, props: Properties): R

    /**
     * @return component types that this supplier can supply
     */
    fun supplyTypes(): List<ComponentType>

    /**
     * @return component rules
     */
    fun rules(): List<ComponentRule> = emptyList()

    /**
     * If component is not defined in storage, can it be apply with [Properties.empty]
     * @return true if component can be apply [Properties.empty], otherwise false
     */
    fun supportNoArgs(): Boolean = false

    /**
     *
     * @return component metadata
     */
    fun metadata(): ComponentMetadata? = null
}

data class ComponentMetadata(
    val description: String? = null,
    val propertySchema: JsonSchema,
    val uiSchema: Map<String, Any> = emptyMap()
)

@Suppress("PropertyName")
data class JsonSchema(
    val `$schema`: String? = null,
    val `$id`: String? = null,
    val `$ref`: String? = null,
    val `$comment`: String? = null,
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val properties: Map<String, JsonSchema>? = null,
    val required: List<String>? = null,
    val enum: List<Any>? = null,
    val const: Any? = null,
    val default: Any? = null,
    val examples: List<Any>? = null,
    val multipleOf: Double? = null,
    val maximum: Double? = null,
    val exclusiveMaximum: Double? = null,
    val minimum: Double? = null,
    val exclusiveMinimum: Double? = null,
    val maxLength: Int? = null,
    val minLength: Int? = null,
    val pattern: String? = null,
    val additionalProperties: Any? = null,
    val items: Any? = null,
    val contains: JsonSchema? = null,
    val maxItems: Int? = null,
    val minItems: Int? = null,
    val uniqueItems: Boolean? = null,
    val maxProperties: Int? = null,
    val minProperties: Int? = null,
    val patternProperties: Map<String, JsonSchema>? = null,
    val dependencies: Map<String, Any>? = null,
    val propertyNames: JsonSchema? = null,
    val ifCondition: JsonSchema? = null,
    val thenCondition: JsonSchema? = null,
    val elseCondition: JsonSchema? = null,
    val allOf: List<JsonSchema>? = null,
    val anyOf: List<JsonSchema>? = null,
    val oneOf: List<JsonSchema>? = null,
    val not: JsonSchema? = null,
    val format: String? = null,
    val contentEncoding: String? = null,
    val contentMediaType: String? = null,
    val `$defs`: Map<String, JsonSchema>? = null
)