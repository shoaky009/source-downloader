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
) : ColumnType<R>(true) where R : Enum<R>, R : EnumValue<T> {


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
    override fun valueFromDB(value: Any): R {
        return enumClass.fromValue(value as T)
    }

    override fun valueToDB(value: R?): Any? {
        return value?.getValue()
    }

}

inline fun <reified R, reified T> Table.enum(name: String): Column<R> where R : Enum<R>, R : EnumValue<T> {
    return registerColumn(name, EnumColumnType(R::class))
}