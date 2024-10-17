package io.github.shoaky.sourcedownloader.application.vertx.handlers

import io.vertx.ext.web.RoutingContext

interface FailureDetailHandler<E : Throwable> {

    fun handle(ctx: RoutingContext, failure: E): ProblemDetail

}