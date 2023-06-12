package io.github.shoaky.sourcedownloader.component

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.sun.net.httpserver.HttpServer
import io.github.shoaky.sourcedownloader.component.supplier.SendHttpRequestSupplier
import io.github.shoaky.sourcedownloader.core.file.CoreSourceContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import kotlin.test.assertEquals

class SendHttpRequestTest {

    @Test
    fun test_query_string() {
        val context = server.createContext("/")
        var queryString: String? = null
        var header: String? = null
        context.setHandler {
            queryString = it.requestURI.query
            header = it.requestHeaders.getFirst("test")
            it.sendResponseHeaders(200, 0)
            it.close()
        }

        val apply = SendHttpRequestSupplier.apply(
            Properties.fromMap(
                mapOf(
                    "url" to "http://localhost:8080?message=下载 {summary}",
                    "method" to "GET",
                    "headers" to mapOf("test" to "test"),
                    "withSummary" to true
                )
            )
        )

        apply.accept(CoreSourceContent(
            sourceItem("test"),
            listOf(),
            MapPatternVariables()
        ))
        assertEquals("message=下载 test内的0个文件", queryString)
        assertEquals("test", header)
    }

    @Test
    fun test_content_body() {
        val context = server.createContext("/body")
        var jsonPath: DocumentContext? = null
        context.setHandler {
            jsonPath = JsonPath.parse(it.requestBody)
            it.sendResponseHeaders(200, 0)
            it.close()
        }

        val apply = SendHttpRequestSupplier.apply(
            Properties.fromMap(
                mapOf(
                    "url" to "http://localhost:8080/body?message=下载 {summary}",
                    "withSummary" to true,
                    "withContent" to true,
                )
            )
        )

        apply.accept(CoreSourceContent(
            sourceItem("test"),
            listOf(),
            MapPatternVariables()
        ))

        assertEquals("test", jsonPath?.read("$.sourceItem.title"))
    }


    @Test
    fun test_custom_body() {
        val context = server.createContext("/custom-body")
        var jsonPath: DocumentContext? = null
        context.setHandler {
            jsonPath = JsonPath.parse(it.requestBody)
            it.sendResponseHeaders(200, 0)
            it.close()
        }

        val apply = SendHttpRequestSupplier.apply(
            Properties.fromMap(
                mapOf(
                    "url" to "http://localhost:8080/custom-body?message=下载 {summary}",
                    "body" to """
                        {
                            "type": "text",
                            "content": "番剧 {summary}"
                        }
                    """.trimIndent(),
                )
            )
        )

        apply.accept(CoreSourceContent(
            sourceItem("test"),
            listOf(),
            MapPatternVariables()
        ))

        assertEquals("text", jsonPath?.read("$.type"))
        assertEquals("番剧 test内的0个文件", jsonPath?.read("$.content"))
    }

    companion object {

        private val server = HttpServer.create(
            InetSocketAddress(8080),
            0
        )

        init {
            server.start()
            println("server started")
        }

        @AfterAll
        @JvmStatic
        fun close() {
            println("close http server")
            server.stop(0)
        }
    }


}