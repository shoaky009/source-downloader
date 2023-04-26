package xyz.shoaky.sourcedownloader.component.trigger

import xyz.shoaky.sourcedownloader.sdk.component.Trigger

abstract class HoldingTaskTrigger : Trigger {

    protected val tasks: MutableList<Runnable> = mutableListOf()

    override fun addTask(task: Runnable) {
        if (tasks.contains(task)) {
            return
        }
        tasks.add(task)
    }

    override fun removeTask(task: Runnable) {
        tasks.remove(task)
    }
}