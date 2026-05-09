package com.example.bobik.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bobik.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val llmProvider by viewModel.llmProvider.collectAsState()
    val llmModel by viewModel.llmModel.collectAsState()
    val llmApiKey by viewModel.llmApiKey.collectAsState()
    val userId by viewModel.userId.collectAsState()
    var editProvider by remember { mutableStateOf(llmProvider) }
    var editModel by remember { mutableStateOf(llmModel) }
    var editApiKey by remember { mutableStateOf(llmApiKey) }
    var showKey by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, null, tint = Color(0xFF00F5FF), modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(10.dp))
            Text("设置", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        // LLM Provider
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("LLM 配置", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))

                // Provider 选择
                Text("Provider", color = Color.Gray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("deepseek" to "DeepSeek", "openai" to "OpenAI", "minimax" to "MiniMax").forEach { (id, label) ->
                        FilterChip(
                            selected = editProvider == id,
                            onClick = { editProvider = id },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7B2FFF),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editModel,
                    onValueChange = { editModel = it },
                    label = { Text("Model") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editApiKey,
                    onValueChange = { editApiKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None
                        else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = Color.Gray
                            )
                        }
                    },
                    colors = fieldColors()
                )

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.configureLLM(editProvider, editModel, editApiKey) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B2FFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存配置")
                }
            }
        }

        // 用户信息
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("用户标识", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(userId, color = Color(0xFFB388FF), fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                Text("Bobik 用此 ID 区分消息来源", color = Color.Gray, fontSize = 11.sp)
            }
        }

        // 关于
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("关于 Bobik", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Bobik 是一个自包含的 Android AI Agent，融合了 Bailongma 数字意识框架的引擎和 Bobi 的客户端界面。\n\n" +
                    "• 内置 TICK 循环 AI 引擎\n" +
                    "• 双层思考架构（L1/L2）\n" +
                    "• 本地 SQLite 记忆系统\n" +
                    "• 支持 DeepSeek / OpenAI / MiniMax\n" +
                    "• 完全离线可运行（仅 LLM 需网络）\n\n" +
                    "版本: 1.0.0",
                    color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF0D0D1A),
    unfocusedContainerColor = Color(0xFF0D0D1A),
    focusedBorderColor = Color(0xFF7B2FFF),
    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color(0xFF00F5FF),
    unfocusedLabelColor = Color.Gray
)
