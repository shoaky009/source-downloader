package xyz.shoaky.sourcedownloader.util

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import org.springframework.util.unit.DataSize

@Component
object DataSizeConvert : Converter<String, DataSize> {
    override fun convert(source: String): DataSize? {
        return DataSize.parse(source)
    }

}