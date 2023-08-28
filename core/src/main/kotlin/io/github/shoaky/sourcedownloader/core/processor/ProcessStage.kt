package io.github.shoaky.sourcedownloader.core.processor

class ProcessStage(
   private val stage: String,
   private val subject: Any?
) {

    override fun toString(): String {
        return "stage:'$stage', subject:$subject"
    }
}