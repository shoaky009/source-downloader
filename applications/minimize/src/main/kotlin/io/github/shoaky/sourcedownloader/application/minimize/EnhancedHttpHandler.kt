package io.github.shoaky.sourcedownloader.application.minimize

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class EnhancedHttpHandler(
    private val block: (HttpExchange) -> Unit
) : HttpHandler {

    override fun handle(exchange: HttpExchange) {
        runCatching {
            block.invoke(exchange)
        }.onFailure {
            exchange.sendResponseHeaders(500, 0)
        }
        exchange.close()
    }
}