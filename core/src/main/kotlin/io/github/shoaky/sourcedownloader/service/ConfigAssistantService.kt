package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.core.ProcessorConfig
import io.github.shoaky.sourcedownloader.core.component.ComponentId
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

class ConfigAssistantService(
    private val metadataService: MetadataService,
    private val componentService: ComponentService,
    private val processorService: ProcessorService,
    private val configOperator: ConfigOperator,
    private val componentManager: ComponentManager,
) {

    fun draft(request: ConfigAssistantDraftRequest): ConfigAssistantDraftResponse {
        val processorMetadata = metadataService.processorMetadata()
        val capabilities = metadataService.componentCapabilities()
        val previousDraft = request.draft
        val normalizedInput = request.input.trim().ifBlank { previousDraft?.normalizedInput.orEmpty() }
        val answers = linkedMapOf<String, Any>().apply {
            previousDraft?.answers?.let { putAll(it) }
            putAll(request.answers)
        }
        val selectedComponentTypes = mergeSelectedComponentTypes(previousDraft, answers)
        val componentDrafts = mergeComponentDrafts(previousDraft, selectedComponentTypes, answers, capabilities)
        val processorDraft = buildProcessorDraft(processorMetadata, selectedComponentTypes, componentDrafts, answers)
        val missingFields = buildMissingFields(
            processorMetadata,
            selectedComponentTypes,
            componentDrafts,
            processorDraft,
            answers,
            capabilities,
        )
        val question = missingFields.firstOrNull()?.let {
            questionFor(it, processorMetadata, capabilities)
        }

        return ConfigAssistantDraftResponse(
            draft = ConfigAssistantDraft(
                intentType = previousDraft?.intentType ?: inferIntentType(normalizedInput),
                summary = summaryOf(normalizedInput, selectedComponentTypes, componentDrafts, processorDraft),
                normalizedInput = normalizedInput,
                answers = answers.toMap(),
                selectedComponentTypes = selectedComponentTypes,
                componentDrafts = componentDrafts,
                processorDraft = if (missingFields.isEmpty()) processorDraft else null,
            ),
            canApply = missingFields.isEmpty(),
            missingFields = missingFields,
            question = question,
        )
    }

    fun apply(request: ConfigAssistantApplyRequest): ConfigAssistantApplyResponse {
        val draft = request.draft
        val processorDraft = draft.processorDraft
            ?: throw IllegalArgumentException("draft is incomplete, processorDraft is missing")

        draft.componentDrafts.forEach { body ->
            val exists = componentManager.getComponent(ComponentType.of(body.type, body.typeName), body.name) != null
            if (!exists) {
                componentService.createComponent(body)
            }
        }
        processorService.create(processorDraft)
        return ConfigAssistantApplyResponse(processorDraft.name)
    }

    private fun buildMissingFields(
        processorMetadata: ProcessorMetadata,
        selectedComponentTypes: Map<String, String>,
        componentDrafts: List<ComponentCreateBody>,
        processorDraft: ProcessorConfig?,
        answers: Map<String, Any>,
        capabilities: List<ComponentCapabilityDetail>,
    ): List<MissingField> {
        val missing = mutableListOf<MissingField>()

        processorMetadata.fields
            .filter { it.askMode != "never" }
            .forEach { field ->
                if (field.required && missingProcessorValue(field, selectedComponentTypes, processorDraft, answers)) {
                    missing.add(field.toMissingField())
                }
            }

        componentDrafts.forEach { draft ->
            val capability = capabilities.firstOrNull {
                it.types.any { type -> type.rootType == draft.type && type.typeName == draft.typeName }
            } ?: return@forEach
            val schema = capability.metadata?.propertySchema ?: return@forEach
            val required = schema.required.orEmpty()
            val properties = schema.properties.orEmpty()
            required.forEach { key ->
                if (!draft.props.containsKey(key) || draft.props[key] == null) {
                    val property = properties[key]
                    missing.add(
                        MissingField(
                            key = "component.${draft.type.primaryName}.${draft.typeName}.${draft.name}.$key",
                            label = property?.title ?: key,
                            description = property?.description
                                ?: "${draft.type.primaryName}:${draft.typeName}:${draft.name} 的必填属性",
                        )
                    )
                }
            }
        }

        return missing.distinctBy { it.key }
    }

    private fun buildProcessorDraft(
        processorMetadata: ProcessorMetadata,
        selectedComponentTypes: Map<String, String>,
        componentDrafts: List<ComponentCreateBody>,
        answers: Map<String, Any>,
    ): ProcessorConfig? {
        val processorName = answers.stringValue("name", "processor.name")
        val sourceType = selectedComponentTypes["source"]
        val sourceName = componentReferenceName("source", sourceType, componentDrafts, answers)
        val downloaderType = selectedComponentTypes["downloader"]
        val downloaderName = componentReferenceName("downloader", downloaderType, componentDrafts, answers)
        val resolverType = selectedComponentTypes["itemFileResolver"]
        val resolverName = componentReferenceName("itemFileResolver", resolverType, componentDrafts, answers)
        val triggerType = selectedComponentTypes["triggers"]
        val triggerName = componentReferenceName("triggers", triggerType, componentDrafts, answers)
        val fileMoverValue = answers.stringValue("fileMover")
            ?: processorMetadata.fields.firstOrNull { it.path == "fileMover" }?.defaultValue?.toString()
            ?: "mover:general"
        val savePath = answers.stringValue("savePath", "processor.savePath")

        if (
            processorName == null ||
            sourceType == null || sourceName == null ||
            downloaderType == null || downloaderName == null ||
            resolverType == null || resolverName == null ||
            savePath == null
        ) {
            return null
        }

        return ProcessorConfig(
            name = processorName,
            enabled = answers["enabled"] as? Boolean ?: true,
            category = answers["category"]?.toString(),
            tags = toStringSet(answers["tags"]),
            triggers = if (triggerType != null && triggerName != null) listOf(ComponentId.from(triggerType, triggerName)) else emptyList(),
            source = ComponentId.from(sourceType, sourceName),
            itemFileResolver = ComponentId.from(resolverType, resolverName),
            downloader = ComponentId.from(downloaderType, downloaderName),
            fileMover = ComponentId(fileMoverValue),
            savePath = savePath,
            options = ProcessorConfig.Options(
                savePathPattern = answers.stringValue("options.savePathPattern", "savePathPattern")
                    ?: processorMetadata.defaultValue("options.savePathPattern", "{item.title}"),
                filenamePattern = answers.stringValue("options.filenamePattern", "filenamePattern")
                    ?: processorMetadata.defaultValue("options.filenamePattern", "{file.name}"),
                variableProviders = toComponentIds(
                    answers["options.variableProviders"] ?: answers["variableProviders"]
                ),
                fetchLimit = toIntValue(answers["options.fetchLimit"] ?: answers["fetchLimit"], 50),
                pointerBatchMode = toBooleanValue(
                    answers["options.pointerBatchMode"] ?: answers["pointerBatchMode"],
                    true,
                ),
                parallelism = toIntValue(answers["options.parallelism"] ?: answers["parallelism"], 1),
            )
        )
    }

    private fun mergeComponentDrafts(
        previousDraft: ConfigAssistantDraft?,
        selectedComponentTypes: Map<String, String>,
        answers: Map<String, Any>,
        capabilities: List<ComponentCapabilityDetail>,
    ): List<ComponentCreateBody> {
        val draftsBySlot = previousDraft?.componentDrafts
            ?.associateBy { slotOf(it.type) }
            ?.toMutableMap()
            ?: linkedMapOf()

        selectedComponentTypes.forEach { (slot, typeName) ->
            val rootType = slotRootType(slot) ?: return@forEach
            val previous = draftsBySlot[slot]
            val capability = capabilities.firstOrNull {
                it.types.any { type -> type.rootType == rootType && type.typeName == typeName }
            }
            val name = answers.stringValue("$slot.name") ?: previous?.name
            val propsSeed = previous?.takeIf { it.type == rootType && it.typeName == typeName }?.props.orEmpty()
            if (name == null) {
                return@forEach
            }
            draftsBySlot[slot] = ComponentCreateBody(
                type = rootType,
                typeName = typeName,
                name = name,
                props = buildComponentProps(slot, typeName, capability, propsSeed, answers),
            )
        }

        return draftsBySlot.values.toList()
    }

    private fun buildComponentProps(
        slot: String,
        typeName: String,
        capability: ComponentCapabilityDetail?,
        existingProps: Map<String, Any>,
        answers: Map<String, Any>,
    ): Map<String, Any> {
        val props = existingProps.toMutableMap()

        capability?.metadata?.propertySchema?.properties.orEmpty().forEach { (key, schema) ->
            val answerKey = listOf(
                "$slot.props.$key",
                "$slot.$key",
                "$typeName.$key",
                key,
            ).firstOrNull { answers[it] != null }
            val value = answerKey?.let { answers[it] } ?: props[key] ?: schema.default
            if (value != null) {
                props[key] = normalizeComponentValue(typeName, key, value)
            }
        }

        return props
    }

    private fun mergeSelectedComponentTypes(
        previousDraft: ConfigAssistantDraft?,
        answers: Map<String, Any>,
    ): Map<String, String> {
        val selected = linkedMapOf<String, String>()
        previousDraft?.selectedComponentTypes?.let { selected.putAll(it) }
        COMPONENT_SLOTS.forEach { slot ->
            val explicit = answers.stringValue(slot, "$slot.type")?.substringBefore(":")
            if (explicit != null) {
                selected[slot] = explicit
            }
        }
        return selected
    }

    private fun inferIntentType(input: String): String {
        return when {
            input.contains("下载") -> "download-workflow"
            else -> "processor-config"
        }
    }

    private fun summaryOf(
        input: String,
        selectedComponentTypes: Map<String, String>,
        componentDrafts: List<ComponentCreateBody>,
        processorDraft: ProcessorConfig?,
    ): String {
        return buildString {
            append("配置草稿")
            if (selectedComponentTypes.isNotEmpty()) {
                append("，已选组件类型: ")
                append(selectedComponentTypes.entries.joinToString { "${it.key}=${it.value}" })
            }
            if (componentDrafts.isNotEmpty()) {
                append("，待创建组件: ")
                append(componentDrafts.joinToString { "${it.type.primaryName}:${it.typeName}:${it.name}" })
            }
            if (processorDraft != null) {
                append("，处理器可应用")
            }
            if (input.isNotBlank()) {
                append("，输入=")
                append(input)
            }
        }
    }

    private fun missingProcessorValue(
        field: ProcessorMetadataField,
        selectedComponentTypes: Map<String, String>,
        processorDraft: ProcessorConfig?,
        answers: Map<String, Any>,
    ): Boolean {
        if (processorDraft != null) {
            return false
        }
        return when (field.path) {
            "triggers" -> {
                val typeName = selectedComponentTypes["triggers"]
                typeName != null && selectedExistingComponentName("triggers", typeName, answers) == null
            }
            "source" -> {
                val typeName = selectedComponentTypes["source"]
                typeName == null || selectedExistingComponentName("source", typeName, answers) == null
            }
            "itemFileResolver" -> {
                val typeName = selectedComponentTypes["itemFileResolver"]
                typeName == null || selectedExistingComponentName("itemFileResolver", typeName, answers) == null
            }
            "downloader" -> {
                val typeName = selectedComponentTypes["downloader"]
                typeName == null || selectedExistingComponentName("downloader", typeName, answers) == null
            }
            else -> answers.noneOfAny(field.path, aliasPath(field.path))
        }
    }

    private fun ProcessorMetadataField.toMissingField(): MissingField {
        return MissingField(
            key = path,
            label = title ?: path,
            description = description ?: "缺少必填字段 $path",
        )
    }

    private fun aliasPath(path: String): String {
        return when (path) {
            "savePath" -> "processor.savePath"
            "name" -> "processor.name"
            "enabled" -> "processor.enabled"
            else -> path
        }
    }

    private fun questionFor(
        field: MissingField,
        processorMetadata: ProcessorMetadata,
        capabilities: List<ComponentCapabilityDetail>,
    ): AssistantQuestion {
        componentPropertyQuestion(field, capabilities)?.let {
            return it
        }
        val processorField = processorMetadata.fields.firstOrNull { it.path == field.key }
        val options = processorField?.let { buildProcessorFieldOptions(it, capabilities) }.orEmpty()
        return AssistantQuestion(
            key = field.key,
            label = processorField?.title ?: field.label,
            description = field.description,
            inputType = questionInputTypeOf(processorField, options),
            required = true,
            multiple = processorField?.type?.endsWith("-array") == true,
            customAllowed = true,
            options = options,
            defaultValue = processorField?.defaultValue,
            placeholder = placeholderOf(field, processorField),
        )
    }

    private fun buildProcessorFieldOptions(
        field: ProcessorMetadataField,
        capabilities: List<ComponentCapabilityDetail>,
    ): List<AssistantQuestionOption> {
        if (field.componentTypes.isNotEmpty() && field.refRootType != null) {
            val existingOptions = existingComponentOptions(field.refRootType, field.componentTypes)
            val createOptions = field.componentTypes.map { typeName ->
                val capability = capabilities.firstOrNull {
                    it.types.any { type -> type.rootType.primaryName == field.refRootType && type.typeName == typeName }
                }
                AssistantQuestionOption(
                    label = typeName,
                    value = typeName,
                    description = capability?.description,
                    recommended = typeName == field.componentTypes.firstOrNull(),
                )
            }
            return existingOptions + createOptions
        }
        return when (field.type) {
            "boolean" -> listOf(
                AssistantQuestionOption("是", "true", recommended = field.defaultValue == true),
                AssistantQuestionOption("否", "false", recommended = field.defaultValue == false),
            )

            else -> emptyList()
        }
    }

    private fun questionInputTypeOf(
        processorField: ProcessorMetadataField?,
        options: List<AssistantQuestionOption>,
    ): AssistantQuestionInputType {
        if (options.isNotEmpty()) {
            return when {
                processorField?.type == "boolean" -> AssistantQuestionInputType.BOOLEAN
                processorField?.type?.endsWith("-array") == true -> AssistantQuestionInputType.MULTI_SELECT
                else -> AssistantQuestionInputType.SINGLE_SELECT
            }
        }
        return when {
            processorField?.type == "boolean" -> AssistantQuestionInputType.BOOLEAN
            else -> AssistantQuestionInputType.TEXT
        }
    }

    private fun placeholderOf(field: MissingField, processorField: ProcessorMetadataField?): String? {
        return when {
            field.key == "savePath" -> "/downloads"
            processorField?.defaultValue != null -> processorField.defaultValue.toString()
            else -> null
        }
    }

    private fun existingComponentOptions(
        refRootType: String,
        componentTypes: List<String>,
    ): List<AssistantQuestionOption> {
        val rootType = ComponentRootType.fromName(refRootType) ?: return emptyList()
        return componentManager.getAllComponent()
            .filter { component ->
                component.type.type == rootType && component.type.typeName in componentTypes
            }
            .sortedBy { it.name }
            .map { component ->
                AssistantQuestionOption(
                    label = "复用 ${component.name}",
                    value = "${component.type.typeName}:${component.name}",
                    description = "复用已有 ${component.type.fullName()} 组件",
                )
            }
    }

    private fun componentPropertyQuestion(
        field: MissingField,
        capabilities: List<ComponentCapabilityDetail>,
    ): AssistantQuestion? {
        val match = Regex("^component\\.([^.]*)\\.([^.]*)\\.([^.]*)\\.(.+)$").matchEntire(field.key) ?: return null
        val rootTypeName = match.groupValues[1]
        val typeName = match.groupValues[2]
        val propertyKey = match.groupValues[4]
        val capability = capabilities.firstOrNull {
            it.types.any { type -> type.rootType.primaryName == rootTypeName && type.typeName == typeName }
        } ?: return null
        val property = capability.metadata?.propertySchema?.properties?.get(propertyKey)
        return questionForComponentProperty(field.key, property, field.description)
    }

    private fun questionForComponentProperty(
        key: String,
        property: JsonSchema?,
        description: String,
    ): AssistantQuestion? {
        property ?: return null
        val options = buildPropertyOptions(property)
        return AssistantQuestion(
            key = key,
            label = property.title ?: key.substringAfterLast('.'),
            description = description,
            inputType = when {
                options.isNotEmpty() -> AssistantQuestionInputType.SINGLE_SELECT
                property.type == "boolean" -> AssistantQuestionInputType.BOOLEAN
                else -> AssistantQuestionInputType.TEXT
            },
            required = true,
            multiple = false,
            customAllowed = true,
            options = options,
            defaultValue = property.default,
            placeholder = property.default?.toString(),
        )
    }

    private fun buildPropertyOptions(property: JsonSchema): List<AssistantQuestionOption> {
        property.const?.let {
            return listOf(
                AssistantQuestionOption(
                    label = it.toString(),
                    value = it.toString(),
                    description = "固定值",
                    recommended = true,
                )
            )
        }
        property.enum?.takeIf { it.isNotEmpty() }?.let { values ->
            return values.mapIndexed { index, value ->
                AssistantQuestionOption(
                    label = value.toString(),
                    value = value.toString(),
                    recommended = index == 0,
                )
            }
        }
        return emptyList()
    }

    private fun componentReferenceName(
        slot: String,
        typeName: String?,
        componentDrafts: List<ComponentCreateBody>,
        answers: Map<String, Any>,
    ): String? {
        val explicit = selectedExistingComponentName(slot, typeName, answers)
        if (explicit != null) {
            return explicit
        }
        return componentDrafts.firstOrNull {
            it.type == slotRootType(slot) && it.typeName == typeName
        }?.name
    }

    private fun selectedExistingComponentName(
        slot: String,
        typeName: String?,
        answers: Map<String, Any>,
    ): String? {
        val explicit = answers.stringValue(slot)?.substringAfter(':', missingDelimiterValue = "")?.ifBlank { null }
            ?: slotAliasNameKey(slot)?.let { answers.stringValue(it) }
            ?: answers.stringValue("$slot.name")
        if (explicit != null) {
            return explicit
        }
        if (typeName == null) {
            return null
        }
        val rootType = slotRootType(slot) ?: return null
        val candidates = componentManager.getAllComponent()
            .filter { it.type.type == rootType && it.type.typeName == typeName }
            .map { it.name }
            .distinct()
        return candidates.singleOrNull()
    }

    private fun slotAliasNameKey(slot: String): String? {
        return when (slot) {
            "triggers" -> "trigger.name"
            else -> null
        }
    }

    private fun slotRootType(slot: String): ComponentRootType? {
        return when (slot) {
            "triggers" -> ComponentRootType.TRIGGER
            "source" -> ComponentRootType.SOURCE
            "downloader" -> ComponentRootType.DOWNLOADER
            "itemFileResolver" -> ComponentRootType.ITEM_FILE_RESOLVER
            "fileMover" -> ComponentRootType.FILE_MOVER
            else -> null
        }
    }

    private fun slotOf(rootType: ComponentRootType): String {
        return when (rootType) {
            ComponentRootType.TRIGGER -> "triggers"
            ComponentRootType.SOURCE -> "source"
            ComponentRootType.DOWNLOADER -> "downloader"
            ComponentRootType.ITEM_FILE_RESOLVER -> "itemFileResolver"
            ComponentRootType.FILE_MOVER -> "fileMover"
            else -> rootType.primaryName
        }
    }

    private fun normalizeComponentValue(typeName: String, key: String, value: Any): Any {
        return value
    }

    private fun ProcessorMetadata.defaultValue(path: String, fallback: String): String {
        return fields.firstOrNull { it.path == path }?.defaultValue?.toString() ?: fallback
    }

    private fun toStringSet(value: Any?): Set<String> {
        return when (value) {
            null -> emptySet()
            is Collection<*> -> value.mapNotNull { it?.toString() }.toSet()
            is String -> value.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            else -> setOf(value.toString())
        }
    }

    private fun toComponentIds(value: Any?): List<ComponentId> {
        return when (value) {
            null -> emptyList()
            is Collection<*> -> value.mapNotNull { it?.toString() }.filter { it.isNotBlank() }.map { ComponentId(it) }
            is String -> value.split(",").map { it.trim() }.filter { it.isNotEmpty() }.map { ComponentId(it) }
            else -> listOf(ComponentId(value.toString()))
        }
    }

    private fun toIntValue(value: Any?, defaultValue: Int): Int {
        return when (value) {
            null -> defaultValue
            is Number -> value.toInt()
            else -> value.toString().toIntOrNull() ?: defaultValue
        }
    }

    private fun toBooleanValue(value: Any?, defaultValue: Boolean): Boolean {
        return when (value) {
            null -> defaultValue
            is Boolean -> value
            else -> value.toString().toBooleanStrictOrNull() ?: defaultValue
        }
    }

    private fun Map<String, Any>.valueOfAny(vararg keys: String): Any? {
        return keys.firstNotNullOfOrNull { this[it] }
    }

    private fun Map<String, Any>.stringValue(vararg keys: String): String? {
        return valueOfAny(*keys)?.toString()?.ifBlank { null }
    }

    private fun Map<String, Any>.noneOfAny(vararg keys: String): Boolean {
        return keys.all { this[it] == null }
    }

    private companion object {

        val COMPONENT_SLOTS = listOf("triggers", "source", "downloader", "itemFileResolver", "fileMover")
    }
}
