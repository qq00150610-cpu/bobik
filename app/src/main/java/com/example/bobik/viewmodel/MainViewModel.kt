package com.example.bobik.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bobik.data.models.*
import com.example.bobik.data.db.MemoryEntity
import com.example.bobik.data.db.ConversationEntity
import com.example.bobik.engine.AgentEngine
import com.example.bobik.engine.EventBus
import com.example.bobik.engine.QuotaTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    // 消息
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // 脑图事件
    private val _brainEvents = MutableStateFlow<List<BrainEvent>>(emptyList())
    val brainEvents: StateFlow<List<BrainEvent>> = _brainEvents.asStateFlow()

    // 引擎状态
    private val _status = MutableStateFlow(UIStatus())
    val status: StateFlow<UIStatus> = _status.asStateFlow()

    // 记忆列表
    private val _memories = MutableStateFlow<List<MemoryItem>>(emptyList())
    val memories: StateFlow<List<MemoryItem>> = _memories.asStateFlow()

    // 输入文本
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // 加载状态
    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    // LLM 配置
    private val _llmProvider = MutableStateFlow(AgentEngine.getLLMConfig().provider)
    val llmProvider: StateFlow<String> = _llmProvider.asStateFlow()
    private val _llmModel = MutableStateFlow(AgentEngine.getLLMConfig().model)
    val llmModel: StateFlow<String> = _llmModel.asStateFlow()
    private val _llmApiKey = MutableStateFlow(AgentEngine.getLLMConfig().apiKey)
    val llmApiKey: StateFlow<String> = _llmApiKey.asStateFlow()

    // 用户 ID
    private val _userId = MutableStateFlow("User-${(1000..9999).random()}")
    val userId: StateFlow<String> = _userId.asStateFlow()

    init {
        // 订阅引擎事件
        EventBus.subscribeAll { event ->
            when (event.type) {
                "response" -> {
                    _messages.value = _messages.value + ChatMessage(
                        fromId = "bobik", role = "assistant",
                        content = event.content, timestamp = event.timestamp
                    )
                    _isThinking.value = false
                }
                "thought", "memory", "action" -> {
                    _brainEvents.value = (_brainEvents.value + BrainEvent(
                        type = event.type, content = event.content,
                        tick = event.tick, timestamp = event.timestamp
                    )).takeLast(100)
                }
                "system", "error" -> {
                    _brainEvents.value = (_brainEvents.value + BrainEvent(
                        type = event.type, content = event.content,
                        tick = event.tick, timestamp = event.timestamp
                    )).takeLast(100)
                }
            }
        }
    }

    fun updateInput(text: String) { _inputText.value = text }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return
        _messages.value = _messages.value + ChatMessage(
            fromId = _userId.value, role = "user", content = text
        )
        _inputText.value = ""
        _isThinking.value = true
        AgentEngine.submitUserMessage(_userId.value, text)
    }

    fun toggleEngine() {
        if (AgentEngine.isRunning) {
            AgentEngine.stop()
        } else {
            AgentEngine.start()
        }
        refreshStatus()
    }

    fun refreshStatus() {
        val stats = AgentEngine.getStats()
        _status.value = UIStatus(
            running = stats.running,
            tickCount = stats.tickCount,
            memoryCount = stats.memoryCount,
            conversationCount = stats.conversationCount,
            provider = stats.provider,
            model = stats.model,
            quotaUsed = stats.quota.used,
            quotaLimit = stats.quota.limit
        )
    }

    fun loadMemories(search: String? = null) {
        val result = if (search.isNullOrBlank()) {
            AgentEngine.getAllMemories()
        } else {
            AgentEngine.searchMemories(search)
        }
        _memories.value = result.map { m: MemoryEntity ->
            MemoryItem(m.id, m.title, m.content, m.category, m.confidence, m.createdAt)
        }
    }

    fun loadRecentConversations(): List<ChatMessage> {
        return AgentEngine.getRecentConversations(30).map { c: ConversationEntity ->
            ChatMessage(fromId = c.fromId, role = c.role, content = c.content, timestamp = c.createdAt.toLongOrNull() ?: 0)
        }.reversed()
    }

    fun configureLLM(provider: String, model: String, apiKey: String) {
        AgentEngine.configureLLM(provider, model, apiKey)
        _llmProvider.value = provider
        _llmModel.value = model
        _llmApiKey.value = apiKey
        refreshStatus()
    }
}
