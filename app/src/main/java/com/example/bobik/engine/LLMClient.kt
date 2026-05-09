package com.example.bobik.engine

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * LLM 客户端，支持多 Provider（MiniMax / DeepSeek / OpenAI）。
 * 移植自 bailongma/src/llm.js，使用 OkHttp 实现流式 SSE 调用。
 */
object LLMClient {
    private val client = okhttp3.OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    data class LLMConfig(
        val provider: String = "deepseek",     // minimax, deepseek, openai
        val model: String = "deepseek-chat",
        val apiKey: String = "",
        val temperature: Double = 0.7,
        val maxTokens: Int = 4096
    )

    data class LLMResponse(
        val content: String,
        val totalTokens: Int = 0,
        val model: String = ""
    )

    fun buildMessages(
        systemPrompt: String,
        conversation: List<Pair<String, String>>,  // role, content
        userMessage: String? = null
    ): org.json.JSONArray {
        val arr = org.json.JSONArray()
        arr.put(org.json.JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })
        conversation.forEach { (role, content) ->
            arr.put(org.json.JSONObject().apply {
                put("role", role)
                put("content", content)
            })
        }
        if (userMessage != null) {
            arr.put(org.json.JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })
        }
        return arr
    }

    fun getEndpoint(config: LLMConfig): String = when (config.provider) {
        "minimax" -> "https://api.minimax.chat/v1/text/chatcompletion_v2"
        "openai" -> "https://api.openai.com/v1/chat/completions"
        else -> "https://api.deepseek.com/v1/chat/completions"
    }

    fun buildRequest(config: LLMConfig, systemPrompt: String, history: List<Pair<String, String>>, userMessage: String): okhttp3.Request {
        val messages = buildMessages(systemPrompt, history, userMessage)
        val body = org.json.JSONObject().apply {
            put("model", config.model)
            put("messages", messages)
            put("temperature", config.temperature)
            put("max_tokens", config.maxTokens)
            put("stream", true)
        }
        return okhttp3.Request.Builder()
            .url(getEndpoint(config))
            .post(body.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .build()
    }

    fun callSync(config: LLMConfig, systemPrompt: String, history: List<Pair<String, String>>, userMessage: String): LLMResponse {
        val nonStreamBody = org.json.JSONObject().apply {
            val messages = buildMessages(systemPrompt, history, userMessage)
            put("model", config.model)
            put("messages", messages)
            put("temperature", config.temperature)
            put("max_tokens", config.maxTokens)
            put("stream", false)
        }
        val request = okhttp3.Request.Builder()
            .url(getEndpoint(config))
            .post(nonStreamBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val text = response.body?.string() ?: ""
            val json = org.json.JSONObject(text)
            val choices = json.getJSONArray("choices")
            val msg = choices.getJSONObject(0).getJSONObject("message")
            return LLMResponse(
                content = msg.getString("content"),
                totalTokens = json.optJSONObject("usage")?.optInt("total_tokens", 0) ?: 0,
                model = json.optString("model", config.model)
            )
        }
    }

    fun callStream(
        config: LLMConfig,
        systemPrompt: String,
        history: List<Pair<String, String>>,
        userMessage: String,
        onDelta: (String) -> Unit,
        onComplete: (LLMResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = buildRequest(config, systemPrompt, history, userMessage)
        Thread {
            try {
                val response = client.newCall(request).execute()
                val reader = java.io.BufferedReader(java.io.InputStreamReader(response.body?.byteStream()))
                var fullContent = StringBuilder()
                var lastChunk = ""

                reader.useLines { lines ->
                    lines.forEach { line ->
                        if (line.startsWith("data: ")) {
                            val data = line.removePrefix("data: ").trim()
                            if (data == "[DONE]") return@forEach
                            try {
                                val json = org.json.JSONObject(data)
                                val choices = json.getJSONArray("choices")
                                if (choices.length() > 0) {
                                    val delta = choices.getJSONObject(0).getJSONObject("delta")
                                    val content = delta.optString("content", "")
                                    if (content.isNotEmpty()) {
                                        fullContent.append(content)
                                        onDelta(content)
                                    }
                                }
                            } catch (_: Exception) {
                                lastChunk = data
                            }
                        }
                    }
                }
                onComplete(LLMResponse(content = fullContent.toString()))
            } catch (e: Exception) {
                onError(e.message ?: "LLM 请求失败")
            }
        }.start()
    }
}
