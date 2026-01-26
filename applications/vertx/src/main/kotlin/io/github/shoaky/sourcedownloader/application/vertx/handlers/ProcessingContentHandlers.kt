package io.github.shoaky.sourcedownloader.application.vertx.handlers

import io.github.shoaky.sourcedownloader.application.vertx.createRouteHandler
import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.repo.ItemCondition
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import io.github.shoaky.sourcedownloader.repo.RangeCondition
import io.github.shoaky.sourcedownloader.service.ProcessingContentService
import io.github.shoaky.sourcedownloader.service.UpdateProcessingContent
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class ProcessingContentHandlers(
    private val service: ProcessingContentService
) {

    fun getProcessingContent(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val id = ctx.pathParam("id")?.toLongOrNull() ?: throw IllegalArgumentException("id is required")
            val content = service.getProcessingContent(id)
            ctx.response().end(JsonObject.mapFrom(content).toBuffer())
        }
    }

    fun queryContents(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val limit = ctx.request().getParam("limit", "20").toIntOrNull() ?: 20
            val maxId = ctx.request().getParam("limit", "0").toLongOrNull() ?: 0L
            val processorName = ctx.request().params().getAll("processorName").takeIf { it.isNotEmpty() }
            val status = ctx.request().params().getAll("status").map {
                ProcessingContent.Status.valueOf(it.uppercase())
            }.takeIf { it.isNotEmpty() }
            val id = ctx.request().params().getAll("id").map { it.toLong() }.takeIf { it.isNotEmpty() }
            val itemHash = ctx.request().getParam("itemHash")
            val createTimeBegin = ctx.request().getParam("createTime.begin", "")
            val createTimeEnd = ctx.request().getParam("createTime.end", "")
            val itemTitle = ctx.request().getParam("item.title")
            // val itemAttrs = ctx.request().getParam("item.attrs")
            // val itemVariables = ctx.request().getParam("item.variables")
            val itemContentType = ctx.request().getParam("item.contentType")
            val itemTags = ctx.request().params().getAll("item.tags").takeIf { it.isNotEmpty() }

            val content = service.queryContents(
                ProcessingQuery(
                    processorName,
                    status,
                    id,
                    itemHash,
                    RangeCondition(
                        createTimeBegin.takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) },
                        createTimeEnd.takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) }
                    ),
                    ItemCondition(
                        title = itemTitle,
                        contentType = itemContentType,
                        tags = itemTags
                    ).takeIf { it.title != null || it.contentType != null || it.tags != null }
                ), limit, maxId
            )
            ctx.response().end(JsonObject.mapFrom(content).toBuffer())
        }
    }

    fun modifyProcessingContent(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val id = ctx.pathParam("id")?.toLongOrNull() ?: throw IllegalArgumentException("id is required")
            ctx.request().bodyHandler {
                val body = it.toJsonObject().mapTo(UpdateProcessingContent::class.java)
                val content = service.modifyProcessingContent(id, body)
                ctx.response().end(JsonObject.mapFrom(content).toBuffer())
            }
        }
    }

    fun deleteProcessingContent(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val id = ctx.pathParam("id")?.toLongOrNull() ?: throw IllegalArgumentException("id is required")
            service.deleteProcessingContent(id)
            ctx.response().statusCode = 204
            ctx.response().end()
        }
    }

    fun reprocess(): Handler<RoutingContext> {
        return createRouteHandler { ctx ->
            val id = ctx.pathParam("id")?.toLongOrNull() ?: throw IllegalArgumentException("id is required")
            // TODO suspend
            runBlocking {
                service.reprocess(id)
            }
            ctx.response().end()
        }
    }
}