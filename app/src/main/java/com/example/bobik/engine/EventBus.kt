package com.example.bobik.engine

/**
 * AI Agent 内部事件总线，替代 bailongma 的 SSE/events 系统。
 * 引擎各组件通过此总线发布事件，UI 层订阅渲染。
 */
object EventBus {
    private val listeners = mutableMapOf<String, MutableList<(Event) -> Unit>>()

    data class Event(
        val type: String,        // thought, memory, action, system, error, tick
        val content: String,
        val tick: Int = 0,
        val metadata: Map<String, String> = emptyMap(),
        val timestamp: Long = System.currentTimeMillis()
    )

    fun subscribe(type: String, listener: (Event) -> Unit) {
        listeners.getOrPut(type) { mutableListOf() }.add(listener)
    }

    fun subscribeAll(listener: (Event) -> Unit) {
        subscribe("*", listener)
    }

    fun publish(type: String, content: String, tick: Int = 0, metadata: Map<String, String> = emptyMap()) {
        val event = Event(type, content, tick, metadata)
        listeners["*"]?.forEach { it(event) }
        listeners[type]?.forEach { it(event) }
    }

    fun clear() {
        listeners.clear()
    }
}
