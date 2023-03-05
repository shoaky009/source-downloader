package xyz.shoaky.sourcedownloader.core.component

import xyz.shoaky.sourcedownloader.sdk.component.Trigger

abstract class TaskHolderTrigger : Trigger {

    protected val tasks: MutableList<Runnable> = mutableListOf()

    override fun addTask(runnable: Runnable) {
        if (tasks.contains(runnable)) {
            return
        }
        tasks.add(runnable)
    }
}