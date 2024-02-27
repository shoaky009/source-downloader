package io.github.shoaky.sourcedownloader.core.component

enum class ComponentFailureType(
    val type: String
) {

    SUPPLIER_NOT_FOUND("supplier_not_found"),
    TYPE_DUPLICATED("type_duplicated"),
    UNKNOWN_TYPE("unknown_type"),
    REQUIRED_PROP_NOT_FOUND("required_prop_not_found"),
    INVALID_PROP("props:invalid"),

    /**
     * The component is not compatible with the other components.
     */
    INCOMPATIBILITY("incompatibility"),
    INSTANCE_NOT_FOUND("instance_not_found"),
    DEFINITION_NOT_FOUND("definition_not_found"),
    PROCESSOR_ALREADY_EXISTS("processor_already_exists"),
    PROCESSOR_NOT_FOUND("processor_not_found"),

}