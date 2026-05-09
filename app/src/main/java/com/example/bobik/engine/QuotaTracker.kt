package com.example.bobik.engine

/**
 * 配额追踪器，移植自 bailongma/src/quota.js。
 * 追踪 LLM token 使用量，支持日/月配额限制。
 */
object QuotaTracker {

    data class QuotaStatus(
        val used: Int = 0,
        val limit: Int = 100000,
        val remaining: Int = 100000,
        val resetDate: String = ""
    )

    private var used = 0
    private var limit = 100000
    private var resetDate = getToday()

    fun configure(newLimit: Int) {
        limit = newLimit
    }

    fun consume(tokens: Int): Boolean {
        checkReset()
        if (used + tokens > limit) return false
        used += tokens
        return true
    }

    fun getStatus(): QuotaStatus {
        checkReset()
        return QuotaStatus(
            used = used,
            limit = limit,
            remaining = (limit - used).coerceAtLeast(0),
            resetDate = resetDate
        )
    }

    fun getAdaptiveTickInterval(): Long {
        val ratio = used.toDouble() / limit.toDouble()
        return when {
            ratio > 0.9 -> 120_000L  // 配额紧张，2 分钟间隔
            ratio > 0.7 -> 60_000L   // 1 分钟
            ratio > 0.5 -> 30_000L   // 30 秒
            else -> 15_000L          // 正常 15 秒
        }
    }

    private fun checkReset() {
        val today = getToday()
        if (today != resetDate) {
            used = 0
            resetDate = today
        }
    }

    fun isRateLimited(): Boolean = used >= limit

    private fun getToday(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
