package xyz.shoaky.sourcedownloader.util

import org.springframework.expression.Expression
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.util.unit.DataSize
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.io.path.notExists
import kotlin.io.path.readAttributes


object SpringExpression {

    private val expressionParser: ExpressionParser = SpelExpressionParser()
    fun parseExpression(expressionText: String): Expression {
        return expressionParser.parseExpression(expressionText)
    }

    fun evalString(expressionText: String, root: Any? = null, variable: Map<String, Any> = emptyMap()): String? {
        val ctx = StandardEvaluationContext()
        ctx.setVariables(variable)
        val expression = expressionParser.parseExpression(expressionText)
        return expression.getValue(root, String::class.java)
    }

}


fun Path.creationTime(): LocalDateTime? {
    if (this.notExists()) {
        return null
    }
    val attrs = this.readAttributes<BasicFileAttributes>()
    val creationTime = attrs.creationTime()
    return LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault())
}

fun Path.lastModifiedTime(): LocalDateTime? {
    if (this.notExists()) {
        return null
    }
    val attrs = this.readAttributes<BasicFileAttributes>()
    val creationTime = attrs.lastModifiedTime()
    return LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault())
}

fun Path.fileDataSize(): DataSize? {
    if (this.notExists()) {
        return null
    }
    val size = Files.size(this)
    return DataSize.ofBytes(size)
}