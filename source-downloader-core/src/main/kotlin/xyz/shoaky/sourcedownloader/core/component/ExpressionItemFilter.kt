package xyz.shoaky.sourcedownloader.core.component

import org.slf4j.LoggerFactory
import org.springframework.expression.BeanResolver
import org.springframework.expression.Expression
import org.springframework.expression.spel.support.StandardEvaluationContext
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.SourceItemFilter
import xyz.shoaky.sourcedownloader.util.SpringExpression
import java.time.LocalDate

class ExpressionItemFilter(
    private val exclusions: List<Expression>,
    private val inclusions: List<Expression>
) : SourceItemFilter {

    override fun test(item: SourceItem): Boolean {
        val variables = mapOf(
            "title" to item.title,
            "contentType" to item.contentType,
            "date" to item.date.toLocalDate(),
            "link" to item.link
        )
        val ctx = StandardEvaluationContext()
        ctx.setBeanResolver(dateBeanResolver)
        ctx.setVariables(variables)

        val all = exclusions.map { it.getValue(ctx, Boolean::class.java) == true }
        if (all.isNotEmpty() && all.any { it }) {
            log.debug("Item {} is excluded by expressions", item)
            return false
        }
        val any = inclusions.map { it.getValue(ctx, Boolean::class.java) == true }.all { it }
        if (any) {
            log.debug("Item {} is included by expressions", item)
            return true
        }
        return false
    }

    companion object {
        private val log = LoggerFactory.getLogger(ExpressionItemFilter::class.java)
        private val dateBeanResolver = BeanResolver { _, beanName ->
            val text = beanName.removePrefix("&")
            LocalDate.parse(text)
        }
    }

}

object ExpressionItemFilterSupplier : SdComponentSupplier<ExpressionItemFilter> {

    override fun apply(props: ComponentProps): ExpressionItemFilter {
        val exclusions = props.getOrDefault<List<String>>("exclusions", listOf())
        val inclusions = props.getOrDefault<List<String>>("inclusions", listOf())
        return expressions(exclusions, inclusions)
    }

    fun expressions(exclusions: List<String> = emptyList(), inclusions: List<String> = emptyList()): ExpressionItemFilter {
        return ExpressionItemFilter(
            exclusions.map { SpringExpression.parseExpression(it) },
            inclusions.map { SpringExpression.parseExpression(it) }
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType("expression", SourceItemFilter::class))
    }

    override fun getComponentClass(): Class<ExpressionItemFilter> {
        return ExpressionItemFilter::class.java
    }

}