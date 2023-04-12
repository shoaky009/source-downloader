package xyz.shoaky.sourcedownloader.util.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import org.springframework.util.unit.DataSize

@Component
private class DataSizeConvert : Converter<String, DataSize> {
    override fun convert(source: String): DataSize {
        return DataSize.parse(source)
    }

}