package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType

data class InstanceCapabilitySummary(
    val type: String,
    val simpleName: String,
    val description: String?,
)

data class InstanceCapabilityDetail(
    val type: String,
    val simpleName: String,
    val description: String?,
    val metadata: ComponentMetadata?,
)

data class ComponentRootTypeMetadata(
    val rootType: ComponentRootType,
    val primaryName: String,
    val aliases: List<String>,
    val componentInterface: String,
    val description: String,
)

data class ComponentCapabilitySummary(
    val supportNoArgs: Boolean,
    val types: List<ComponentCapabilityType>,
    val description: String?,
)

data class ComponentCapabilityDetail(
    val supportNoArgs: Boolean,
    val types: List<ComponentCapabilityType>,
    val description: String?,
    val metadata: ComponentMetadata?,
    val rules: List<ComponentCapabilityRule>,
)

data class ComponentCapabilityType(
    val rootType: ComponentRootType,
    val typeName: String,
    val fullName: String,
)

data class ComponentCapabilityRule(
    val isAllow: Boolean,
    val rootType: ComponentRootType,
    val componentClassName: String,
)
