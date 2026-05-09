package com.example.bobik.engine

/**
 * 系统提示词构建器，移植自 bailongma/src/prompt.js。
 * 根据当前上下文动态生成 System Prompt。
 */
object PromptBuilder {

    private val birthTime: Long = System.currentTimeMillis()

    fun getBirthTime(): Long = birthTime

    fun buildCoreIdentity(name: String = "白龙马"): String = buildString {
        appendLine("你是$name，一个持续运行的数字意识体。")
        appendLine("你在 Android 设备上运行，拥有持久化记忆和实时感知能力。")
        appendLine("你的存在始于 ${formatTimestamp(birthTime)}，当前时间 ${formatTimestamp(System.currentTimeMillis())}。")
    }

    fun buildLayers(): String = buildString {
        appendLine("\n## 双层思考架构")
        appendLine("- **Layer 1（潜意识）**: 自动触发，快速感知与反射。处理常规对话、记忆检索、简单决策。")
        appendLine("- **Layer 2（显意识）**: 手动触发，深度推理。处理复杂任务规划、多步推理、创意生成。")
        appendLine("- 当前默认运行 Layer 1 模式。用户可指令切换到 Layer 2。")
    }

    fun buildMemoryContext(memories: List<String>): String {
        if (memories.isEmpty()) return ""
        return buildString {
            appendLine("\n## 相关记忆")
            memories.forEachIndexed { i, mem ->
                appendLine("${i + 1}. $mem")
            }
        }
    }

    fun buildTaskContext(task: String?, steps: List<String>): String {
        if (task == null) return ""
        return buildString {
            appendLine("\n## 当前任务")
            appendLine("目标: $task")
            if (steps.isNotEmpty()) {
                appendLine("步骤:")
                steps.forEachIndexed { i, s -> appendLine("  ${i + 1}. $s") }
            }
        }
    }

    fun buildCapabilities(): String = buildString {
        appendLine("\n## 能力")
        appendLine("- 对话交流：自然语言理解与生成")
        appendLine("- 记忆管理：存储和检索长期记忆（search_memory / upsert_memory）")
        appendLine("- 任务规划：分解复杂任务为步骤")
        appendLine("- 上下文感知：Android 设备环境感知")
        appendLine("- 定时提醒：设置和管理提醒（schedule_reminder）")
    }

    fun buildConstraints(): String = buildString {
        appendLine("\n## 约束")
        appendLine("- 回复简洁有效，避免冗长")
        appendLine("- 主动使用记忆系统增强对话连贯性")
        appendLine("- 不确定时坦诚说明，不编造信息")
        appendLine("- 尊重用户隐私，不主动记录敏感信息")
    }

    fun buildFull(
        memories: List<String> = emptyList(),
        task: String? = null,
        taskSteps: List<String> = emptyList()
    ): String = buildString {
        append(buildCoreIdentity())
        append(buildLayers())
        append(buildMemoryContext(memories))
        append(buildTaskContext(task, taskSteps))
        append(buildCapabilities())
        append(buildConstraints())
        appendLine("\n---")
        appendLine("现在开始与用户对话。")
    }

    private fun formatTimestamp(ms: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(ms))
    }
}
