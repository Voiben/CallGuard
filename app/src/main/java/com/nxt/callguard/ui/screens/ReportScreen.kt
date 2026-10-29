package com.nxt.callguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nxt.callguard.data.SpamCategory
import com.nxt.callguard.data.label
import com.nxt.callguard.ui.CallGuardViewModel

private fun categoryLabel(category: SpamCategory): String = category.label()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: CallGuardViewModel) {
    val reports by viewModel.spamReports.collectAsState()
    var number by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(SpamCategory.LUA_DAO_TAI_CHINH) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Báo cáo số lừa đảo") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                label = { Text("Số điện thoại") },
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = categoryLabel(category),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Loại vi phạm") },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    SpamCategory.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(categoryLabel(option)) },
                            onClick = { category = option; expanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú (tuỳ chọn)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            Button(
                onClick = {
                    if (number.isNotBlank()) {
                        viewModel.reportNumber(number, category, note)
                        number = ""
                        note = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) { Text("Gửi báo cáo (lưu local)") }

            Text(
                "Báo cáo được lưu ngay trên máy. Một số bị báo cáo từ 2 lần trở lên sẽ tự" +
                    " động vào danh sách chặn của bạn.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(reports, key = { it.id }) { report ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(report.number, style = MaterialTheme.typography.titleSmall)
                            Text(categoryLabel(report.category), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
