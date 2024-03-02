package io.github.shoaky.sourcedownloader.component.trigger

import io.github.shoaky.sourcedownloader.sdk.ProcessTask
import io.github.shoaky.sourcedownloader.sdk.component.ComponentStateful
import io.github.shoaky.sourcedownloader.sdk.component.Trigger
import org.slf4j.LoggerFactory

abstract class HoldingTaskTrigger : Trigger, ComponentStateful {

    protected val tasks: MutableList<ProcessTask> = mutableListOf()

    override fun addTask(task: ProcessTask) {
        if (tasks.contains(task)) {
            return
        }
        tasks.add(task)
    }

    override fun removeTask(task: ProcessTask): Boolean {
        return tasks.remove(task)
    }

    protected fun getSourceGroupingTasks(): List<Runnable> {
        return tasks.groupBy { it.group ?: "default" }
            .map { (_, v) ->
                TaskGroup(v.map { it.runnable })
            }
    }

    private class TaskGroup(
        private val tasks: List<Runnable>
    ) : Runnable {

        override fun run() {
            for (task in tasks) {
                try {
                    task.run()
                } catch (e: Exception) {
                    log.error("任务处理发生异常:{}", task, e)
                }
            }
        }
    }

    override fun stateDetail(): Any {
        val tasks = tasks
            .groupBy({ it.group ?: "default" }, {
                mapOf("processName" to it.processName)
            })
        return mapOf(
            "tasks" to tasks,
        )
    }

    companion object {

        private val log = LoggerFactory.getLogger(HoldingTaskTrigger::class.java)
    }
}