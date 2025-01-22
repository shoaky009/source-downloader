package io.github.shoaky.sourcedownloader.component.trigger

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.ticker
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.ForkJoinPool

/**
 * 固定时间间隔触发器
 */
class FixedScheduleTrigger(
    private val interval: Duration,
    private val onStartRunTasks: Boolean = false,
) : HoldingTaskTrigger() {

    @OptIn(ObsoleteCoroutinesApi::class)
    private val tickerChannel = ticker(interval.toMillis(), interval.toMillis())
    private var job: Job? = null

    override fun stop() {
        tickerChannel.cancel()
        job?.cancel()
    }

    override fun start() {
        job = CoroutineScope(ForkJoinPool.commonPool().asCoroutineDispatcher()).launch {
            tickerChannel.consumeEach {
                getSourceGroupingTasks().forEach { task ->
                    Thread.ofVirtual().name("fixed-schedule-$interval")
                        .start(task)
                        .setUncaughtExceptionHandler { _, e ->
                            log.error("任务处理发生异常:{}", task, e)
                        }
                }
            }
        }
        if (onStartRunTasks) {
            getSourceGroupingTasks().forEach {
                Thread.ofVirtual().name("onstart-up-$interval")
                    .start {
                        it.run()
                    }
            }
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger(FixedScheduleTrigger::class.java)
    }

}

