package io.github.shoaky.sourcedownloader.application.spring.converter

import io.github.shoaky.sourcedownloader.core.component.ComponentFailureType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.throwComponentException
import org.springframework.core.convert.converter.Converter

class ComponentsConverter : Converter<String, ComponentTopType> {

    override fun convert(source: String): ComponentTopType {
        return ComponentTopType.fromName(source) ?: throwComponentException(
            "未知组件类型:$source",
            ComponentFailureType.UNKNOWN_TYPE
        )
    }
}