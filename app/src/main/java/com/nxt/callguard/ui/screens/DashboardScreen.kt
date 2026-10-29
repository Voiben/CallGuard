package com.nxt.callguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.nxt.callguard.ui.CallGuardViewModel

@Composable
fun DashboardScreen(viewModel: CallGuardViewModel) {
    val blockedToday by viewModel.blockedToday.collectAsState()
    val blocklistSize by viewModel.blockedNumbers.collectAsState()
    val reports by viewModel.spamReports.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("CallGuard") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Đã chặn hôm nay", blockedToday.toString())
            StatCard("Số trong danh sách chặn", blocklistSize.size.toString())
            StatCard("Báo cáo cộng đồng (local)", reports.size.toString())
            Text(
                "Lưu ý: để chặn cuộc gọi tự động, hãy vào Cài đặt > đặt CallGuard làm " +
                    "ứng dụng Caller ID & chặn spam mặc định.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
