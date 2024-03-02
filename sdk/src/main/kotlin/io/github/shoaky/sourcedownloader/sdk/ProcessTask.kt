package io.github.shoaky.sourcedownloader.sdk

class ProcessTask(
    val processName: String,
    val runnable: Runnable,
    /**
     * For grouping tasks
     */
    val group: String? = null
) : Runnable by runnable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProcessTask) return false

        if (processName != other.processName) return false

        return true
    }

    override fun hashCode(): Int {
        return processName.hashCode()
    }
}