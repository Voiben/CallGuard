package com.nxt.callguard.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nxt.callguard.data.ActivityAction
import com.nxt.callguard.ui.CallGuardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

@Composable
fun HistoryScreen(viewModel: CallGuardViewModel) {
    val activity by viewModel.recentActivity.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Lịch sử") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(activity, key = { it.id }) { entry ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${entry.number} · ${entry.type}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            actionLabel(entry.action) + " — " + entry.detail,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(timeFormat.format(Date(entry.timestamp)), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

private fun actionLabel(action: ActivityAction): String = when (action) {
    ActivityAction.BLOCKED -> "Đã chặn"
    ActivityAction.ALLOWED -> "Cho qua"
    ActivityAction.FLAGGED -> "Gắn cờ nghi ngờ"
}
