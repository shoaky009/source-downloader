package io.github.shoaky.sourcedownloader.util.converter

import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
private class ComponentsConverter : Converter<String, ComponentTopType> {

    override fun convert(source: String): ComponentTopType {
        return ComponentTopType.fromName(source) ?: throw ComponentException.unsupported("未知组件类型:$source")
    }
}