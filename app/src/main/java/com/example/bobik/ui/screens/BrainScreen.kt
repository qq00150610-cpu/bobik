package com.example.bobik.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bobik.data.models.BrainEvent
import com.example.bobik.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun BrainScreen(viewModel: MainViewModel) {
    val events by viewModel.brainEvents.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(events.size) {
        if (events.isNotEmpty()) {
            listState.animateScrollToItem(events.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(color = Color(0xFF1A1A2E)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Psychology, null, tint = Color(0xFF00F5FF), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("脑图", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("实时意识事件流 · ${events.size} 条", color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    coroutineScope.launch { listState.animateScrollToItem(events.size - 1) }
                }) {
                    Text("最新", color = Color(0xFF00F5FF), fontSize = 12.sp)
                }
            }
        }

        Divider(color = Color.White.copy(alpha = 0.1f))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (events.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Psychology, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("等待意识事件...", color = Color.Gray)
                        }
                    }
                }
            }
            items(events.reversed()) { event ->
                BrainEventCard(event)
            }
        }
    }
}

@Composable
fun BrainEventCard(event: BrainEvent) {
    val eventColor = when (event.type) {
        "thought" -> Color(0xFF00F5FF)
        "memory" -> Color(0xFFB388FF)
        "action" -> Color(0xFFFFAB40)
        "response" -> Color(0xFF00E676)
        "error" -> Color(0xFFFF5252)
        "system" -> Color(0xFF9E9E9E)
        "tick" -> Color(0xFF448AFF)
        else -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = eventColor.copy(alpha = 0.2f)
            ) {
                Text(
                    event.type,
                    color = eventColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.content.take(250),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
            Spacer(Modifier.width(4.dp))
            Text("#${event.tick}", color = Color.Gray, fontSize = 10.sp)
        }
    }
}
