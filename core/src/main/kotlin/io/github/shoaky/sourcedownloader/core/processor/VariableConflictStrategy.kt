package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

enum class VariableConflictStrategy {
    /**
     * 任意一个，目前是按定义的顺序
     */
    ANY,

    /**
     * 值相同的数量最多的
     */
    VOTE,

    /**
     * 根据[VariableProvider.accuracy]排序，精确度越高越优先
     */
    ACCURACY,

    /**
     * VOTE + ACCURACY
     */
    SMART
}