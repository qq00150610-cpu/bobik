package com.example.bobik.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bobik.viewmodel.MainViewModel

@Composable
fun StatusScreen(viewModel: MainViewModel) {
    val status by viewModel.status.collectAsState()

    LaunchedEffect(Unit) { viewModel.refreshStatus() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Monitor, null, tint = Color(0xFF00F5FF), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text("系统状态", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { viewModel.toggleEngine() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status.running) Color(0xFFFF5252) else Color(0xFF00E676)
                    )
                ) {
                    Text(if (status.running) "停止引擎" else "启动引擎", fontSize = 13.sp)
                }
            }
        }

        // 引擎状态
        item {
            StatusCard(
                icon = if (status.running) Icons.Default.PlayArrow else Icons.Default.Stop,
                iconColor = if (status.running) Color(0xFF00E676) else Color(0xFFFF5252),
                title = "引擎状态",
                content = if (status.running) "AI 意识循环运行中" else "已停止",
                details = listOf(
                    "当前 TICK" to "#${status.tickCount}",
                    "LLM" to "${status.provider}/${status.model}"
                )
            )
        }

        // 数据统计
        item {
            StatusCard(
                icon = Icons.Default.Storage,
                iconColor = Color(0xFF00F5FF),
                title = "数据统计",
                content = "本地 SQLite 持久化存储",
                details = listOf(
                    "记忆数" to "${status.memoryCount}",
                    "对话记录" to "${status.conversationCount}"
                )
            )
        }

        // 配额
        item {
            StatusCard(
                icon = Icons.Default.DataUsage,
                iconColor = Color(0xFFFFAB40),
                title = "LLM 配额",
                content = "",
                details = listOf(
                    "已用" to "${status.quotaUsed} tokens",
                    "上限" to "${status.quotaLimit} tokens",
                    "剩余" to "${(status.quotaLimit - status.quotaUsed).coerceAtLeast(0)} tokens"
                )
            )
        }

        // 说明
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("关于引擎", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Bobik 内置完整的 AI Agent 引擎（移植自 Bailongma 框架）。\n\n" +
                        "• TICK 循环：周期性自主思考\n" +
                        "• 双层架构：Layer 1 潜意识 + Layer 2 显意识\n" +
                        "• 记忆系统：持久化存储 + 语义检索\n" +
                        "• 多 LLM 支持：DeepSeek / MiniMax / OpenAI\n\n" +
                        "全部数据存储在本地 SQLite 数据库，无需外部服务器。",
                        color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    content: String,
    details: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            if (content.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(content, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                details.forEach { (label, value) ->
                    Column {
                        Text(label, color = Color.Gray, fontSize = 11.sp)
                        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
