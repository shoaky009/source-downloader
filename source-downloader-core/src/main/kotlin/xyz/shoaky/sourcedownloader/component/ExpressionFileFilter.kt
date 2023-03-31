package xyz.shoaky.sourcedownloader.component

import org.slf4j.LoggerFactory
import org.springframework.expression.BeanResolver
import org.springframework.expression.Expression
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.util.unit.DataSize
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.SourceFileFilter
import xyz.shoaky.sourcedownloader.util.SpringExpression
import xyz.shoaky.sourcedownloader.util.creationTime
import xyz.shoaky.sourcedownloader.util.fileDataSize
import xyz.shoaky.sourcedownloader.util.lastModifiedTime
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name


class ExpressionFileFilter(
    private val exclusions: List<Expression>,
    private val inclusions: List<Expression>
) : SourceFileFilter {
    override fun test(path: Path): Boolean {
        val creationTime = path.creationTime()
        val lastModifiedTime = path.lastModifiedTime()
        val variables = mapOf(
            "filename" to path.name,
            "size" to path.fileDataSize(),
            "contentType" to Files.probeContentType(path),
            "creationTime" to creationTime,
            "lastModifiedTime" to lastModifiedTime
        )

        val ctx = StandardEvaluationContext()
        ctx.setVariables(variables)
        ctx.setBeanResolver(dataSizeBeanResolver)
        val all = exclusions.map { it.getValue(ctx, Boolean::class.java) == true }
        if (all.isNotEmpty() && all.any { it }) {
            log.debug("File {} is excluded by expressions", path)
            return false
        }
        val any = inclusions.map { it.getValue(ctx, Boolean::class.java) == true }.all { it }
        if (any) {
            log.debug("File {} is included by expressions", path)
            return true
        }
        return false
    }

    companion object {
        private val dataSizeBeanResolver = BeanResolver { _, beanName -> DataSize.parse(beanName.removePrefix("&")) }
        private val log = LoggerFactory.getLogger(ExpressionFileFilter::class.java)
    }

}

object ExpressionFileFilterSupplier : SdComponentSupplier<ExpressionFileFilter> {
    override fun apply(props: ComponentProps): ExpressionFileFilter {
        val exclusions = props.getOrDefault<List<String>>("exclusions", listOf())
        val inclusions = props.getOrDefault<List<String>>("inclusions", listOf())
        return expressions(exclusions, inclusions)
    }

    fun expressions(exclusions: List<String> = emptyList(), inclusions: List<String> = emptyList()): ExpressionFileFilter {
        return ExpressionFileFilter(
            exclusions.map { SpringExpression.parseExpression(it) },
            inclusions.map { SpringExpression.parseExpression(it) }
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileFilter("expression")
        )
    }

    override fun getComponentClass(): Class<ExpressionFileFilter> {
        return ExpressionFileFilter::class.java
    }

}