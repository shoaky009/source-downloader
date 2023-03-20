package xyz.shoaky.sourcedownloader.core.component

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
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
            ComponentProps.fromMap(
                mapOf(
                    "url" to "http://localhost:8080?message=下载{summary}",
                    "method" to "GET",
                    "headers" to mapOf("test" to "test"),
                    "withSummary" to true
                )
            )
        )

        apply.accept(SourceContent(
            sourceItem("test"),
            listOf()
        ))
        server.stop(0)
        assertEquals("message=下载test内的0个文件", queryString)
    }

}