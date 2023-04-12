package xyz.shoaky.sourcedownloader.util.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.sdk.component.ComponentException
import xyz.shoaky.sourcedownloader.sdk.component.Components

@Component
private class ComponentsConverter : Converter<String, Components> {
    override fun convert(source: String): Components {
        return Components.fromName(source) ?: throw ComponentException.unsupported("未知组件类型:$source")
    }
}