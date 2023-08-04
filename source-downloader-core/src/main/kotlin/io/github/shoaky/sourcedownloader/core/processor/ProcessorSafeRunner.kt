package io.github.shoaky.sourcedownloader.core.processor

class ProcessorSafeRunner(
    private val processor: SourceProcessor
) : Runnable {

    @Volatile
    private var running = false
    override fun run() {
        val name = processor.name
        log.info("Processor:'${name}' 触发获取源信息")
        if (running) {
            log.info("Processor:'${name}' 上一次任务还未完成，跳过本次任务")
            return
        }
        running = true
        try {
            processor.run()
        } catch (e: Exception) {
            log.error("Processor:'${name}' 执行失败", e)
        } finally {
            running = false
        }
    }

}