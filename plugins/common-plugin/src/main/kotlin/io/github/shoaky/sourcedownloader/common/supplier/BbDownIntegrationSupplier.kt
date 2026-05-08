package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.bilibili.BbDownIntegration
import io.github.shoaky.sourcedownloader.common.bilibili.BbDownOptions
import io.github.shoaky.sourcedownloader.common.bilibili.BilibiliSource
import io.github.shoaky.sourcedownloader.external.bbdown.BbDownClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object BbDownIntegrationSupplier : ComponentSupplier<BbDownIntegration> {

    override fun apply(context: CoreContext, props: Properties): BbDownIntegration {
        return BbDownIntegration(
            props.get("download-path"),
            BbDownClient(props.get("endpoint")),
            props.getOrDefault("options", BbDownOptions())
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("bbdown"),
            ComponentType.fileResolver("bbdown")
        )
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allow(ComponentRootType.SOURCE, BilibiliSource::class)
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("download-path", "endpoint"),
                properties = mapOf(
                    "download-path" to JsonSchema(type = "string"),
                    "endpoint" to JsonSchema(type = "string", format = "uri"),
                    "options" to JsonSchema(
                        type = "object",
                        properties = mapOf(
                            "cookie" to JsonSchema(type = "string"),
                            "filePattern" to JsonSchema(type = "string", default = "<videoTitle>_<bvid>"),
                            "workDir" to JsonSchema(type = "string"),
                            "selectPage" to JsonSchema(type = "string"),
                            "multiFilePattern" to JsonSchema(type = "string", default = "<videoTitle>_<bvid>_p<pageNumberWithZero>"),
                            "dfnPriority" to JsonSchema(type = "string"),
                            "downloadDanmaku" to JsonSchema(type = "boolean")
                        )
                    )
                )
            )
        )
    }
}
