package com.example.bobik.engine

import android.content.Context
import com.example.bobik.data.db.*
import kotlinx.coroutines.*

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

    private val pendingUserMessages = mutableListOf<String>()
    private var pendingUserId: String = "local"

    fun init(context: Context) {
        if (isInitialized) return
        db = AppDatabase.getInstance(context)
        memoryDao = db.memoryDao()
        conversationDao = db.conversationDao()
        configDao = db.configDao()

        llmConfig = LLMClient.LLMConfig(
            provider = configDao.get("llm_provider") ?: "deepseek",
            model = configDao.get("llm_model") ?: "deepseek-chat",
            apiKey = configDao.get("llm_api_key") ?: "",
            temperature = configDao.get("llm_temperature")?.toDoubleOrNull() ?: 0.7,
            maxTokens = configDao.get("llm_max_tokens")?.toIntOrNull() ?: 4096
        )

        val quotaLimit = configDao.get("quota_limit")?.toIntOrNull() ?: 100000
        QuotaTracker.configure(quotaLimit)

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

    fun configureLLM(provider: String, model: String, apiKey: String, temperature: Double = 0.7, maxTokens: Int = 4096) {
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
        TickDriver.start { tick -> tickCycle(tick) }
        EventBus.publish("system", "Agent 引擎启动，TICK 间隔自适应")
    }

    fun stop() {
        TickDriver.stop()
        isRunning = false
        EventBus.publish("system", "Agent 引擎已停止")
    }

    fun submitUserMessage(userId: String, message: String) {
        pendingUserId = userId
        pendingUserMessages.add(message)
        CoroutineScope(Dispatchers.IO).launch { processMessage(message) }
    }

    fun searchMemories(query: String, limit: Int = 10): List<MemoryEntity> = memoryDao.search(query, limit)
    fun getRecentConversations(limit: Int = 30): List<ConversationEntity> = conversationDao.getRecent(limit)
    fun getAllMemories(limit: Int = 100): List<MemoryEntity> = memoryDao.getAll(limit)

    fun getStats(): EngineStats = EngineStats(
        running = isRunning,
        tickCount = TickDriver.getTickCount(),
        memoryCount = memoryDao.getCount(),
        conversationCount = conversationDao.getCount(),
        provider = llmConfig.provider,
        model = llmConfig.model,
        quota = QuotaTracker.getStatus()
    )

    data class EngineStats(
        val running: Boolean, val tickCount: Int, val memoryCount: Int,
        val conversationCount: Int, val provider: String, val model: String,
        val quota: QuotaTracker.QuotaStatus
    )

    private suspend fun tickCycle(tick: Int) {
        if (pendingUserMessages.isEmpty() && tick % 5 == 0) autoReflect(tick)
    }

    private suspend fun processMessage(message: String) {
        try {
            conversationDao.insert(ConversationEntity(fromId = "local", role = "user", content = message, createdAt = System.currentTimeMillis().toString()))
            EventBus.publish("thought", "收到用户消息，开始思考...", tick = TickDriver.getTickCount())

            val related = memoryDao.search(message, 5)
            val memoryTexts = related.map { "${it.title}: ${it.content.take(100)}" }
            val history = conversationDao.getRecent(20).map { it.role to it.content }
            val systemPrompt = PromptBuilder.buildFull(memories = memoryTexts, task = currentTask, taskSteps = taskSteps)

            val fullResponse = StringBuilder()
            suspendCancellableCoroutine<Unit> { cont ->
                LLMClient.callStream(
                    config = llmConfig, systemPrompt = systemPrompt, history = history, userMessage = message,
                    onDelta = { delta ->
                        fullResponse.append(delta)
                        EventBus.publish("thought", delta, tick = TickDriver.getTickCount(), metadata = mapOf("streaming" to "true"))
                    },
                    onComplete = { response ->
                        val content = fullResponse.toString().ifBlank { response.content }
                        conversationDao.insert(ConversationEntity(fromId = "bobik", role = "assistant", content = content, createdAt = System.currentTimeMillis().toString()))
                        EventBus.publish("response", content, tick = TickDriver.getTickCount())
                        if (response.totalTokens > 0) QuotaTracker.consume(response.totalTokens)
                        cont.resume(Unit) {}
                    },
                    onError = { error ->
                        EventBus.publish("error", "LLM 错误: $error", tick = TickDriver.getTickCount())
                        conversationDao.insert(ConversationEntity(fromId = "bobik", role = "assistant", content = "抱歉，思考时出了点问题: $error", createdAt = System.currentTimeMillis().toString()))
                        cont.resume(Unit) {}
                    }
                )
            }
        } catch (e: Exception) {
            EventBus.publish("error", "处理消息异常: ${e.message}", tick = TickDriver.getTickCount())
        }
    }

    private suspend fun autoReflect(tick: Int) {
        try {
            val recentConv = conversationDao.getRecent(5)
            if (recentConv.isEmpty() || recentConv.lastOrNull()?.role == "user") return
            EventBus.publish("thought", "自主反思中...", tick = tick)
        } catch (_: Exception) {}
    }
}
