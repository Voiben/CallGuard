package com.nxt.callguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nxt.callguard.ui.CallGuardViewModel
import com.nxt.callguard.util.ContactsHelper

@Composable
fun BlocklistScreen(viewModel: CallGuardViewModel) {
    val numbers by viewModel.blockedNumbers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Danh sách chặn") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm số")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(numbers, key = { it.number }) { entry ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(entry.number, style = MaterialTheme.typography.titleMedium)
                            Text(entry.reason, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.removeFromBlocklist(entry) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Xoá")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddNumberDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { number, reason ->
                viewModel.addToBlocklist(number, reason)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddNumberDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var number by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isContact by androidx.compose.runtime.produceState(initialValue = false, number) {
        value = number.length >= 6 &&
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                ContactsHelper.isKnownContact(context, number)
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm số vào danh sách chặn") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Số điện thoại") })
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Lý do (tuỳ chọn)") })
                if (isContact) {
                    Text(
                        "⚠️ Số này đang có trong Danh bạ máy. Android sẽ KHÔNG chặn cuộc gọi " +
                            "từ số đã lưu trong Danh bạ, dù bạn thêm vào đây — hãy xoá khỏi " +
                            "Danh bạ nếu muốn chặn thật.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (number.isNotBlank()) onConfirm(number, reason) }) { Text("Thêm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
