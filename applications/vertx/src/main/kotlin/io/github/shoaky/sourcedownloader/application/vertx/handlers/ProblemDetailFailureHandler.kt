package io.github.shoaky.sourcedownloader.application.vertx.handlers

import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

object ProblemDetailFailureHandler : Handler<RoutingContext> {

    private val failureDetailHandlerChain:
        MutableMap<out Throwable, FailureDetailHandler<in Throwable>> = mutableMapOf()

    override fun handle(ctx: RoutingContext) {
        if (ctx.response().ended() || ctx.response().headWritten()) {
            log.debug("Response already ended or head written, skip handling failure")
            return
        }
        val retDetail = retDetail(ctx) ?: ProblemDetail.internalServerError()
        val buf = JsonObject.mapFrom(retDetail).toBuffer()
        ctx.response().statusCode = retDetail.status
        ctx.response().end(buf)
    }

    private fun retDetail(ctx: RoutingContext): ProblemDetail? {
        val failure = ctx.failure()
        val type = failure::class
        for ((exception, handler) in failureDetailHandlerChain.entries) {
            if (type.isInstance(exception)) {
                return handler.handle(ctx, failure)
            }
        }
        return null
    }

    private val log = LoggerFactory.getLogger(ProblemDetailFailureHandler::class.java)
}