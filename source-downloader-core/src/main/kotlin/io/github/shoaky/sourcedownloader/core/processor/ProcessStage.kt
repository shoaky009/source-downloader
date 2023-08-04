package io.github.shoaky.sourcedownloader.core.processor

class ProcessStage(
    val stage: String,
    val subject: Any?
) {

    override fun toString(): String {
        return "stage:'$stage', subject:$subject"
    }
}