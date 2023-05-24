package xyz.shoaky.sourcedownloader.sdk.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.assertEquals

class ApiClientTest {

    private val endpoint: URI = URI.create("http://127.0.0.1:35500")

    init {

        server
    }

    @Test
    fun test_get() {
        val context = server.createContext("/get/test")
        context.setHandler { ex ->
            val queryString = ex.requestURI.query
            val split = queryString.split("&")
            val body = split.map { it.split("=") }.associate { it[0] to it[1] }.toMutableMap()
            ex.requestHeaders.forEach { t, u ->
                body[t] = u[0]
            }

            ex.responseHeaders.add("Content-Type", "application/json")
            ex.sendResponseHeaders(200, 0)
            ex.responseBody.use { os ->
                os.write(Jackson.toJsonString(body).toByteArray())
            }
            ex.close()
        }


        val request = GetRequest("test", 1)
        request.queryString["queryString"] = "queryString"
        request.setHeader("header", "getRequest")
        val execute = Client.execute(endpoint, request)
        val body = execute.body()

        assertEquals("test", body["query"])
        assertEquals("1", body["page"])
        assertEquals("getRequest", body["Header"])
        assertEquals("beforeRequest", body["Hooked"])
        assertEquals("queryString", body["queryString"])
    }

    @Test
    fun test_post() {
        val context = server.createContext("/other/test")
        context.setHandler { ex ->
            val queryString = ex.requestURI.query
            val split = queryString.split("&")
            val queryStringHeader = split.map { it.split("=") }.associate { it[0] to it[1] }.toMutableMap()
            ex.requestHeaders.forEach { t, u ->
                queryStringHeader[t] = u[0]
            }
            val body = Jackson.fromJson(ex.requestBody, jacksonTypeRef<MutableMap<String, Any>>())
            body.putAll(queryStringHeader)

            ex.responseHeaders.add("Content-Type", "application/json")
            ex.sendResponseHeaders(200, 0)
            ex.responseBody.use { os ->
                os.write(Jackson.toJsonString(body).toByteArray())
            }
            ex.close()
        }

        val request = OtherRequest(
            mapOf("data" to "data"),
            "data2"
        )
        request.queryString["queryString"] = "queryString"
        request.setHeader("header", "getRequest")
        val execute = Client.execute(endpoint, request)
        val body = execute.body()

        assertEquals("data2", body["data2"])
        assertEquals("queryString", body["queryString"])
        assertEquals("getRequest", body["Header"])
        assertEquals("data", Jackson.convert(body["data"]!!, jacksonTypeRef<MutableMap<String, Any>>())["data"])
    }

    companion object {

        private val server = HttpServer.create(
            InetSocketAddress(35500),
            0
        )

        init {
            server.start()
        }

        @AfterAll
        @JvmStatic
        fun close() {
            println("close http server")
            server.stop(0)
        }
    }
}

private object Client : HookedApiClient() {
    override fun <R : BaseRequest<T>, T : Any> beforeRequest(requestBuilder: HttpRequest.Builder, request: R) {
        request.setHeader("hooked", "beforeRequest")
    }

    override fun <R : BaseRequest<T>, T : Any> afterRequest(response: HttpResponse<T>, request: R) {
    }

}

private data class GetRequest(
    val query: String,
    val page: Int
) : BaseRequest<Map<String, Any>>() {

    override val path: String = "/get/test"
    override val responseBodyType: TypeReference<Map<String, Any>> = jacksonTypeRef()
    override val httpMethod: HttpMethod = HttpMethod.GET
    override val mediaType: MediaType? = null

}

private data class OtherRequest(
    val data: Map<String, Any>,
    val data2: String
) : BaseRequest<Map<String, Any>>() {
    override val path: String = "/other/test"
    override val responseBodyType: TypeReference<Map<String, Any>> = jacksonTypeRef()
    override val httpMethod: HttpMethod = HttpMethod.POST
    override val mediaType: MediaType = MediaType.JSON_UTF_8
}