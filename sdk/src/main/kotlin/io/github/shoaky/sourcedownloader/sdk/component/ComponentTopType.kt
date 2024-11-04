package io.github.shoaky.sourcedownloader.sdk.component

import com.fasterxml.jackson.annotation.JsonValue
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

enum class ComponentTopType(
    val klass: KClass<out SdComponent>,
    val alias: List<String>,
) {

    TRIGGER(Trigger::class, listOf("trigger")),
    SOURCE(Source::class, listOf("source")),
    DOWNLOADER(Downloader::class, listOf("downloader")),
    ITEM_FILE_RESOLVER(
        ItemFileResolver::class,
        listOf("item-file-resolver", "file-resolver", "itemFileResolver", "fileResolver")
    ),
    FILE_MOVER(FileMover::class, listOf("file-mover", "mover", "fileMover")),
    VARIABLE_PROVIDER(VariableProvider::class, listOf("variable-provider", "provider", "variableProvider")),
    PROCESS_LISTENER(ProcessListener::class, listOf("process-listener", "listener", "run-after-completion")),
    SOURCE_ITEM_FILTER(
        SourceItemFilter::class,
        listOf("item-filter", "source-item-filter", "sourceItemFilter", "itemFilter")
    ),
    ITEM_CONTENT_FILTER(
        ItemContentFilter::class,
        listOf("item-content-filter", "itemContentFilter")
    ),
    FILE_CONTENT_FILTER(
        FileContentFilter::class,
        listOf("file-filter", "file-content-filter", "fileContentFilter", "fileFilter")
    ),
    TAGGER(FileTagger::class, listOf("file-tagger", "tagger")),
    FILE_REPLACEMENT_DECIDER(
        FileReplacementDecider::class,
        listOf("file-replacement-decider", "replacement-decider", "fileReplacementDecider", "replacementDecider")
    ),
    FILE_EXISTS_DETECTOR(
        FileExistsDetector::class,
        listOf("file-exists-detector", "exists-detector", "fileExistsDetector", "existsDetector")
    ),
    VARIABLE_REPLACER(
        VariableReplacer::class,
        listOf("variable-replacer", "variableReplacer")
    ),
    MANUAL_SOURCE(
        ManualSource::class,
        listOf("manual-source")
    )
    ;

    @JsonValue
    val primaryName = alias.first()

    companion object {

        private val nameMapping: Map<String, ComponentTopType> = entries.flatMap {
            it.alias.map { name -> name to it }
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