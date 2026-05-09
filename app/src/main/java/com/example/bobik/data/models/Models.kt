package com.example.bobik.data.models

// UI 层使用的聊天消息
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fromId: String = "",
    val role: String = "user",  // user / assistant / system
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// 脑图实时事件
data class BrainEvent(
    val type: String = "",
    val content: String = "",
    val tick: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

// 引擎状态（UI 展示用）
data class UIStatus(
    val running: Boolean = false,
    val tickCount: Int = 0,
    val memoryCount: Int = 0,
    val conversationCount: Int = 0,
    val provider: String = "",
    val model: String = "",
    val quotaUsed: Int = 0,
    val quotaLimit: Int = 100000
)

// 记忆条目
data class MemoryItem(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val confidence: Double = 0.0,
    val createdAt: String = ""
)
