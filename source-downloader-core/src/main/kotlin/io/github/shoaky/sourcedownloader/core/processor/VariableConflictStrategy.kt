package io.github.shoaky.sourcedownloader.core.processor

enum class VariableConflictStrategy {
    ANY,
    VOTE,
    ACCURACY,

    /**
     * VOTE + ACCURACY
     */
    SMART
}