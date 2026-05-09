package com.example.bobik.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bobik.data.models.MemoryItem
import com.example.bobik.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesScreen(viewModel: MainViewModel) {
    val memories by viewModel.memories.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadMemories() }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = Color(0xFF1A1A2E)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, null, tint = Color(0xFF00F5FF), modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("记忆库", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.weight(1f))
                    Text("${memories.size} 条", color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { q -> searchQuery = q; viewModel.loadMemories(q.ifBlank { null }) },
                    placeholder = { Text("搜索记忆...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF00F5FF)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF16213E),
                        unfocusedContainerColor = Color(0xFF16213E),
                        focusedBorderColor = Color(0xFF7B2FFF),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Divider(color = Color.White.copy(alpha = 0.1f))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (memories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无记忆", color = Color.Gray)
                    }
                }
            }
            items(memories) { mem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (mem.category) {
                                    "article" -> Color(0xFF00F5FF).copy(alpha = 0.2f)
                                    "task" -> Color(0xFFFFAB40).copy(alpha = 0.2f)
                                    "identity" -> Color(0xFF00E676).copy(alpha = 0.2f)
                                    else -> Color(0xFF7B2FFF).copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    mem.category.ifEmpty { "general" },
                                    fontSize = 10.sp,
                                    color = when (mem.category) {
                                        "article" -> Color(0xFF00F5FF)
                                        "task" -> Color(0xFFFFAB40)
                                        "identity" -> Color(0xFF00E676)
                                        else -> Color(0xFFB388FF)
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            if (mem.confidence > 0) Text(
                                "置信度: ${(mem.confidence * 100).toInt()}%",
                                fontSize = 10.sp, color = Color.Gray
                            )
                        }
                        if (mem.title.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(mem.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        if (mem.content.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                mem.content.take(200), color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp, maxLines = 4, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
