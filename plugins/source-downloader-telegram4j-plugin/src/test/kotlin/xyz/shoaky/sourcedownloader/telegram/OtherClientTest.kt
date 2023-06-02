package xyz.shoaky.sourcedownloader.telegram

import io.netty.util.ResourceLeakDetector
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import reactor.core.publisher.Hooks
import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.telegram.other.ChatConfig
import xyz.shoaky.sourcedownloader.telegram.other.TelegramClientInstanceFactory
import xyz.shoaky.sourcedownloader.telegram.other.TelegramIntegration
import xyz.shoaky.sourcedownloader.telegram.other.TelegramSource
import kotlin.io.path.Path


@Disabled
class OtherClientTest {

    @Test
    fun name() {
        Hooks.onOperatorDebug()
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID)

        val client = TelegramClientInstanceFactory.create(
            Properties.fromMap(
                mapOf(
                    "api-id" to "1",
                    "api-hash" to "1",
                    "proxy" to "http://localhost:8888",
                    "metadata-path" to "src/test/resources"
                )
            )
        )

        val integration = TelegramIntegration(client, Path("/Users/shoaky/temp/downloads"))
        val source = TelegramSource(client, listOf(ChatConfig(775236548L)))
        val fetch = source.fetch(null).toList()
        for (pointedItem in fetch) {
            val sourceItem = pointedItem.sourceItem
            println(sourceItem)
            val resolveFiles = integration.resolveFiles(sourceItem)
            println(resolveFiles)
            println(integration.createSourceGroup(sourceItem).sharedPatternVariables().variables())

            val task = DownloadTask(sourceItem,
                resolveFiles.map { Path("/Users/shoaky/temp/downloads").resolve(it.path) },
                Path("/Users/shoaky/temp/downloads"))
            // integration.submit(task)
        }
    }
}