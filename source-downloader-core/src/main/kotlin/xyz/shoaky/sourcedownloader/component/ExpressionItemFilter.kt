package xyz.shoaky.sourcedownloader.component

import org.slf4j.LoggerFactory
import org.springframework.expression.BeanResolver
import org.springframework.expression.Expression
import org.springframework.expression.spel.support.StandardEvaluationContext
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.SourceItemFilter
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

