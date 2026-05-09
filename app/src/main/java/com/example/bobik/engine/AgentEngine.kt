package com.example.bobik.engine

import android.content.Context
import com.example.bobik.data.db.AppDatabase
import com.example.bobik.data.db.ConversationDao
import com.example.bobik.data.db.MemoryDao
import com.example.bobik.data.db.ConfigDao
import kotlinx.coroutines.*

/**
 * AI Agent 引擎主控制器。
 * 融合 bailongma 的 TICK 循环 + 记忆系统 + LLM 集成。
 * 这是整个 bobik 的核心——自包含的 AI 意识引擎。
 */
object AgentEngine {
    private lateinit var db: AppDatabase
    private lateinit var memoryDao: MemoryDao
    private lateinit var conversationDao: ConversationDao
    private lateinit var configDao: ConfigDao

    var isInitialized = false
        private set
    var isRunning = false
        private set

    private var llmConfig = LLMClient.LLMConfig()
    private var currentTask: String? = null
    private var taskSteps: List<String> = emptyList()

    // Runtime context gathered each tick
    private val pendingUserMessages = mutableListOf<String>()
    private var pendingUserId: String = "local"

    fun init(context: Context) {
        if (isInitialized) return
        db = AppDatabase.getInstance(context)
        memoryDao = db.memoryDao()
        conversationDao = db.conversationDao()
        configDao = db.configDao()

        // 恢复 LLM 配置
        llmConfig = LLMClient.LLMConfig(
            provider = configDao.get("llm_provider") ?: "deepseek",
            model = configDao.get("llm_model") ?: "deepseek-chat",
            apiKey = configDao.get("llm_api_key") ?: "",
            temperature = configDao.get("llm_temperature")?.toDoubleOrNull() ?: 0.7,
            maxTokens = configDao.get("llm_max_tokens")?.toIntOrNull() ?: 4096
        )

        // 恢复配额
        val quotaLimit = configDao.get("quota_limit")?.toIntOrNull() ?: 100000
        QuotaTracker.configure(quotaLimit)

        // 恢复任务
        currentTask = configDao.get("current_task")
        val rawSteps = configDao.get("current_task_steps")
        if (!rawSteps.isNullOrBlank()) {
            try {
                taskSteps = org.json.JSONArray(rawSteps).let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }
                }
            } catch (_: Exception) {}
        }

        isInitialized = true
        EventBus.publish("system", "引擎初始化完成，LLM Provider: ${llmConfig.provider}")
    }

    fun configureLLM(
        provider: String, model: String, apiKey: String,
        temperature: Double = 0.7, maxTokens: Int = 4096
    ) {
        llmConfig = LLMClient.LLMConfig(provider, model, apiKey, temperature, maxTokens)
        configDao.set("llm_provider", provider)
        configDao.set("llm_model", model)
        configDao.set("llm_api_key", apiKey)
        configDao.set("llm_temperature", temperature.toString())
        configDao.set("llm_max_tokens", maxTokens.toString())
        EventBus.publish("system", "LLM 配置已更新: $provider / $model")
    }

    fun getLLMConfig() = llmConfig

    fun start() {
        if (!isInitialized) throw IllegalStateException("引擎未初始化")
        if (isRunning) return
        isRunning = true
        TickDriver.start { tick ->
            tickCycle(tick)
        }
        EventBus.publish("system", "Agent 引擎启动，TICK 间隔自适应")
    }

    fun stop() {
        TickDriver.stop()
        isRunning = false
        EventBus.publish("system", "Agent 引擎已停止")
    }

    /**
     * 提交用户消息（从聊天 UI 调用）
     */
    fun submitUserMessage(userId: String, message: String) {
        pendingUserId = userId
        pendingUserMessages.add(message)

        // 立即触发一次思考
        CoroutineScope(Dispatchers.IO).launch {
            processMessage(message)
        }
    }

    /**
     * 搜索记忆
     */
    fun searchMemories(query: String, limit: Int = 10): List<MemoryEntry> {
        return memoryDao.search(query, limit)
    }

    /**
     * 获取最近对话
     */
    fun getRecentConversations(limit: Int = 30): List<ConversationEntry> {
        return conversationDao.getRecent(limit)
    }

    /**
     * 获取所有记忆
     */
    fun getAllMemories(limit: Int = 100): List<MemoryEntry> {
        return memoryDao.getAll(limit)
    }

    /**
     * 获取统计信息
     */
    fun getStats(): EngineStats {
        return EngineStats(
            running = isRunning,
            tickCount = TickDriver.getTickCount(),
            memoryCount = memoryDao.getCount(),
            conversationCount = conversationDao.getCount(),
            provider = llmConfig.provider,
            model = llmConfig.model,
            quota = QuotaTracker.getStatus()
        )
    }

    // ===== 内部实现 =====

    private suspend fun tickCycle(tick: Int) {
        // 自动思考（Layer 1）
        if (pendingUserMessages.isEmpty() && tick % 5 == 0) {
            autoReflect(tick)
        }
    }

    private suspend fun processMessage(message: String) {
        try {
            // 保存用户消息
            conversationDao.insert(ConversationEntry(0, "local", "user", message, System.currentTimeMillis().toString()))
            EventBus.publish("thought", "收到用户消息，开始思考...", tick = TickDriver.getTickCount())

            // 搜索相关记忆
            val relatedMemories = memoryDao.search(message, 5)
            val memoryTexts = relatedMemories.map { "${it.title}: ${it.content.take(100)}" }

            // 构建提示词
            val history = conversationDao.getRecent(20).map { it.role to it.content }
            val systemPrompt = PromptBuilder.buildFull(
                memories = memoryTexts,
                task = currentTask,
                taskSteps = taskSteps
            )

            // 调用 LLM（流式）
            val fullResponse = StringBuilder()

            suspendCancellableCoroutine<Unit> { cont ->
                LLMClient.callStream(
                    config = llmConfig,
                    systemPrompt = systemPrompt,
                    history = history,
                    userMessage = message,
                    onDelta = { delta ->
                        fullResponse.append(delta)
                        EventBus.publish("thought", delta, tick = TickDriver.getTickCount(),
                            metadata = mapOf("streaming" to "true"))
                    },
                    onComplete = { response ->
                        val content = fullResponse.toString().ifBlank { response.content }
                        // 保存 AI 回复
                        conversationDao.insert(ConversationEntry(0, "bobik", "assistant", content, System.currentTimeMillis().toString()))
                        EventBus.publish("response", content, tick = TickDriver.getTickCount())

                        // 更新配额
                        if (response.totalTokens > 0) {
                            QuotaTracker.consume(response.totalTokens)
                        }
                        cont.resume(Unit) {}
                    },
                    onError = { error ->
                        EventBus.publish("error", "LLM 错误: $error", tick = TickDriver.getTickCount())
                        conversationDao.insert(ConversationEntry(0, "bobik", "assistant", "抱歉，思考时出了点问题: $error", System.currentTimeMillis().toString()))
                        cont.resume(Unit) {}
                    }
                )
            }
        } catch (e: Exception) {
            EventBus.publish("error", "处理消息异常: ${e.message}", tick = TickDriver.getTickCount())
        }
    }

    private suspend fun autoReflect(tick: Int) {
        // Layer 1 自动反思：基于最近对话和记忆的自主思考
        try {
            val recentConv = conversationDao.getRecent(5)
            if (recentConv.isEmpty()) return

            val lastMsg = recentConv.lastOrNull() ?: return
            if (lastMsg.role == "user") return  // 有未回复的用户消息，让 processMessage 处理

            EventBus.publish("thought", "自主反思中...", tick = tick)
            // 简化版：只发布事件，不实际调用 LLM（避免配额浪费）
        } catch (_: Exception) {}
    }

    // ===== 数据类 =====

    data class EngineStats(
        val running: Boolean,
        val tickCount: Int,
        val memoryCount: Int,
        val conversationCount: Int,
        val provider: String,
        val model: String,
        val quota: QuotaTracker.QuotaStatus
    )

    // Room entities 在 data/db 中定义，这里用简化的 data class 给外部使用
    data class MemoryEntry(
        val id: Long = 0,
        val memId: String = "",
        val category: String = "",
        val title: String = "",
        val content: String = "",
        val confidence: Double = 0.0,
        val createdAt: String = ""
    )

    data class ConversationEntry(
        val id: Long = 0,
        val fromId: String = "",
        val role: String = "",
        val content: String = "",
        val createdAt: String = ""
    )
}
