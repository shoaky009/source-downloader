package xyz.shoaky.sourcedownloader.component

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.SendHttpRequestSupplier
import xyz.shoaky.sourcedownloader.core.file.PersistentSourceContent
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sourceItem
import java.net.InetSocketAddress
import kotlin.test.assertEquals

class SendHttpRequestTest {

    private val server = HttpServer.create(
        InetSocketAddress(8080),
        0
    )

    init {
        server.start()
    }

    @Test
    fun normal() {
        val context = server.createContext("/")
        var queryString: String? = null
        context.setHandler {
            queryString = it.requestURI.query
            println(it.requestURI)
            it.sendResponseHeaders(200, 0)
            it.close()
        }

        val apply = SendHttpRequestSupplier.apply(
            Properties.fromMap(
                mapOf(
                    "url" to "http://localhost:8080?message=下载{summary}",
                    "method" to "GET",
                    "headers" to mapOf("test" to "test"),
                    "withSummary" to true
                )
            )
        )

        apply.accept(PersistentSourceContent(
            sourceItem("test"),
            listOf(),
            MapPatternVariables()
        ))
        server.stop(0)
        assertEquals("message=下载test内的0个文件", queryString)
    }

}