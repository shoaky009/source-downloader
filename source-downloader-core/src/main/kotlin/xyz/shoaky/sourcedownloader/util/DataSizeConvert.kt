package xyz.shoaky.sourcedownloader.util

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import org.springframework.util.unit.DataSize

@Component
object DataSizeConvert : Converter<String, DataSize> {
    override fun convert(source: String): DataSize {
        return DataSize.parse(source)
    }

}

fun String.find(vararg regexes: Regex): String? {
    for (regex in regexes) {
        val match = regex.find(this)
        if (match != null) {
            return match.value
        }
    }
    return null
}