package io.github.shoaky.sourcedownloader.core.expression

enum class VariableType {
    STRING,
    BOOLEAN,

    /**
     * 没有范型描述默认String, Any
     */
    MAP,

    /**
     * 没有范型描述默认Any
     */
    ARRAY,
    DATE,
    ANY
}