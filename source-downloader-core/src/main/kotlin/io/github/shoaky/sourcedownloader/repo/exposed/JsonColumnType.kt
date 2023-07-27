package io.github.shoaky.sourcedownloader.repo.exposed

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.vendors.SQLiteDialect
import org.jetbrains.exposed.sql.vendors.currentDialect

class JsonColumnType<T : Any>(
    private val json: TypeReference<T>,
    val type: Type = Type.JSONB
) : StringColumnType() {

    override fun sqlType(): String = when (type) {
        Type.JSON -> "JSON"
        Type.JSONB -> "JSONB"
        Type.TEXT -> currentDialect.dataTypeProvider.textType()
    }

    override fun valueFromDB(value: Any) = when (val v = super.valueFromDB(value)) {
        is String -> Jackson.fromJson(v, json)
        else -> v
    }

    override fun notNullValueToDB(value: Any): Any = when (currentDialect) {
        is SQLiteDialect -> Jackson.toJsonString(value)
        else -> error("Unsupported dialect: $currentDialect")
    }

    override fun nonNullValueToString(value: Any) = Jackson.toJsonString(value)

    override fun valueToString(value: Any?): String = when (value) {
        is Iterable<*> -> nonNullValueToString(value)
        else -> super.valueToString(value)
    }

    enum class Type {

        JSON, JSONB, TEXT

    }

}

inline fun <reified T : Any> Table.json(name: String) = registerColumn<T>(name, JsonColumnType(jacksonTypeRef<T>()))

class JsonValue<T>(
    val expr: Expression<*>,
    override val columnType: ColumnType,
    val jsonPath: List<String>
) : Function<T>(columnType) {

    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        val json = (columnType is JsonColumnType<*>) && (columnType.type != JsonColumnType.Type.TEXT)
        if (json) append("(")
        append(expr)
        append(" #>")
        if (json) append(">")
        append(" '{${jsonPath.joinToString { escapeFieldName(it) }}}'")
        if (json) append(")::${columnType.sqlType()}")
    }

    private fun escapeFieldName(value: String) = value
        .map {
            when (it) {
                '\"' -> "\\\""
                '\r' -> "\\r"
                '\n' -> "\\n"
                else -> it
            }
        }.joinToString("").let { "\"$it\"" }

}

inline fun <reified T : Any> Column<*>.json(vararg jsonPath: String): JsonValue<T> {
    val columnType = when (T::class) {
        Boolean::class -> BooleanColumnType()
        Byte::class -> ByteColumnType()
        Short::class -> ShortColumnType()
        Int::class -> IntegerColumnType()
        Long::class -> LongColumnType()
        Float::class -> FloatColumnType()
        Double::class -> DoubleColumnType()
        String::class -> TextColumnType()
        else ->
            JsonColumnType(jacksonTypeRef<T>())
    }
    return JsonValue(this, columnType, jsonPath.toList())
}

class JsonContainsOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "??")

infix fun <T> JsonValue<Any>.contains(t: T): JsonContainsOp =
    JsonContainsOp(this, SqlExpressionBuilder.run { wrap(t) })

infix fun <T> JsonValue<Any>.contains(other: Expression<T>): JsonContainsOp =
    JsonContainsOp(this, other)