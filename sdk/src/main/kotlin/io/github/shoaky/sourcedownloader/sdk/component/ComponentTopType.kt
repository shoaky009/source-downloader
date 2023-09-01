package io.github.shoaky.sourcedownloader.sdk.component

import com.fasterxml.jackson.annotation.JsonValue
import com.google.common.base.CaseFormat
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

enum class ComponentTopType(
    val klass: KClass<out SdComponent>,
    val names: List<String>
) {

    TRIGGER(Trigger::class, listOf("trigger")),
    SOURCE(Source::class, listOf("source")),
    DOWNLOADER(Downloader::class, listOf("downloader")),
    ITEM_FILE_RESOLVER(
        ItemFileResolver::class,
        listOf("item-file-resolver", "file-resolver", "itemFileResolver", "fileResolver")
    ),
    VARIABLE_PROVIDER(VariableProvider::class, listOf("provider", "variable-provider", "variableProvider")),
    FILE_MOVER(FileMover::class, listOf("mover", "file-mover", "fileMover")),
    RUN_AFTER_COMPLETION(RunAfterCompletion::class, listOf("run-after-completion", "run", "runAfterCompletion")),
    SOURCE_ITEM_FILTER(
        SourceItemFilter::class,
        listOf("source-item-filter", "item-filter", "sourceItemFilter", "itemFilter")
    ),
    SOURCE_CONTENT_FILTER(
        ItemContentFilter::class,
        listOf("item-content-filter", "content-filter", "itemContentFilter", "contentFilter")
    ),
    FILE_CONTENT_FILTER(
        FileContentFilter::class,
        listOf("file-content-filter", "file-filter", "fileContentFilter", "fileFilter")
    ),
    TAGGER(FileTagger::class, listOf("file-tagger", "tagger")),
    FILE_REPLACEMENT_DECIDER(
        FileReplacementDecider::class,
        listOf("file-replacement-decider", "replacement-decider", "fileReplacementDecider", "replacementDecider")
    ),
    ITEM_EXISTS_DETECTOR(
        FileExistsDetector::class,
        listOf("item-exists-detector", "exists-detector", "itemExistsDetector", "existsDetector")
    ),
    MANUAL_SOURCE(
        ManualSource::class,
        listOf("manual-source")
    )
    ;

    @JsonValue
    fun lowerHyphenName(): String {
        return CaseFormat.UPPER_UNDERSCORE.to(
            CaseFormat.LOWER_HYPHEN,
            this.name
        )
    }

    companion object {

        private val nameMapping: Map<String, ComponentTopType> = entries.flatMap {
            it.names.map { name -> name to it }
        }.toMap()

        fun fromClass(klass: KClass<out SdComponent>): List<ComponentTopType> {
            if (klass == SdComponent::class) {
                throw ComponentException.other("can not create instance of SdComponent.class")
            }

            return entries.filter {
                it.klass.isSuperclassOf(klass)
            }
        }

        fun fromName(name: String): ComponentTopType? {
            return nameMapping[name]
        }

    }
}