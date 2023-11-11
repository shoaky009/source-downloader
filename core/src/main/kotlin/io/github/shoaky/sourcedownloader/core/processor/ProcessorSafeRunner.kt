package io.github.shoaky.sourcedownloader.core.processor

import java.util.concurrent.atomic.AtomicBoolean

class ProcessorSafeRunner(
    private val processor: SourceProcessor
) : Runnable {

    private val running = AtomicBoolean(false)
    override fun run() {
        val name = processor.name
        log.info("Processor:'$name' 触发获取源信息")
        if (running.compareAndSet(false, true).not()) {
            log.info("Processor:'$name' 上一次任务还未完成，跳过本次任务")
            return
        }
        try {
            processor.run()
        } catch (e: Exception) {
            log.error("Processor:'$name' 执行失败", e)
        } finally {
            running.set(false)
        }
    }

}