package io.github.shoaky.sourcedownloader.repo.exposed

import io.github.shoaky.sourcedownloader.util.EnumValue
import io.github.shoaky.sourcedownloader.util.fromValue
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.currentDialect
import kotlin.reflect.KClass

class EnumColumnType<R, T>(
    private val enumClass: KClass<R>
) : ColumnType(
) where R : Enum<R>, R : EnumValue<T> {

    override fun sqlType(): String {
        val value = enumClass.java.enumConstants.first()
        return when (value.getValue() as Any) {
            is Int -> currentDialect.dataTypeProvider.integerType()
            is String -> currentDialect.dataTypeProvider.textType()
            is Short -> currentDialect.dataTypeProvider.shortType()
            is Byte -> currentDialect.dataTypeProvider.byteType()
            else -> throw IllegalArgumentException("Unsupported enum value type: $value")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun valueFromDB(value: Any): Any {
        return enumClass.fromValue(value as T)
    }

    @Suppress("UNCHECKED_CAST")
    override fun notNullValueToDB(value: Any): Any {
        return (value as R).getValue() as Any
    }

    override fun nonNullValueToString(value: Any): String {
        return value.toString()
    }
}

inline fun <reified R, reified T> Table.enum(name: String): Column<R> where R : Enum<R>, R : EnumValue<T> {
    return registerColumn(name, EnumColumnType(R::class))
}