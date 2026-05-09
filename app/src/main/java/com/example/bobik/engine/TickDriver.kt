package com.example.bobik.engine

/**
 * TICK 循环驱动器，移植自 bailongma 的主循环。
 * 这是整个 AI Agent 的心脏，周期性执行感知-思考-行动循环。
 */
object TickDriver {
    private var tickCount = 0
    private var running = false
    private var tickCallback: (suspend (Int) -> Unit)? = null
    private var thread: Thread? = null
    private var intervalMs: Long = 15_000L

    fun getTickCount(): Int = tickCount
    fun isRunning(): Boolean = running

    fun setInterval(ms: Long) {
        intervalMs = ms.coerceIn(5000, 300000)
    }

    fun start(callback: suspend (Int) -> Unit) {
        if (running) return
        running = true
        tickCallback = callback
        thread = Thread {
            while (running) {
                tickCount++
                EventBus.publish("tick", "TICK #$tickCount", tick = tickCount)
                try {
                    kotlinx.coroutines.runBlocking {
                        tickCallback?.invoke(tickCount)
                    }
                } catch (e: Exception) {
                    EventBus.publish("error", "TICK 异常: ${e.message}", tick = tickCount)
                }
                // 自适应间隔
                val adaptiveMs = QuotaTracker.getAdaptiveTickInterval()
                val actualMs = minOf(intervalMs, adaptiveMs)
                Thread.sleep(actualMs)
            }
        }.apply {
            isDaemon = true
            name = "bobik-tick"
            start()
        }
    }

    fun stop() {
        running = false
        thread?.interrupt()
        thread = null
        tickCallback = null
    }
}
