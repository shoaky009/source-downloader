package xyz.shoaky.sourcedownloader.component

import xyz.shoaky.sourcedownloader.sdk.component.Trigger

abstract class HoldingTaskTrigger : Trigger {

    protected val tasks: MutableList<Runnable> = mutableListOf()

    override fun addTask(runnable: Runnable) {
        if (tasks.contains(runnable)) {
            return
        }
        tasks.add(runnable)
    }
}