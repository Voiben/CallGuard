package com.nxt.callguard.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nxt.callguard.BuildConfig
import com.nxt.callguard.util.UpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    onRequestRuntimePermissions: () -> Unit,
    onRequestCallScreeningRole: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text("Cài đặt") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "CallGuard cần 2 bước để hoạt động đầy đủ:",
                style = MaterialTheme.typography.titleMedium
            )

            Text("1. Cấp quyền đọc cuộc gọi/SMS cho app.", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onRequestRuntimePermissions, modifier = Modifier.fillMaxWidth()) {
                Text("Cấp quyền")
            }

            Text(
                "2. Đặt CallGuard làm ứng dụng chặn cuộc gọi mặc định (Call Screening)," +
                    " bắt buộc để tự động từ chối số trong danh sách chặn.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onRequestCallScreeningRole, modifier = Modifier.fillMaxWidth()) {
                Text("Đặt làm app chặn cuộc gọi mặc định")
            }

            Text(
                "Lưu ý: CallGuard không phải ứng dụng SMS mặc định nên chỉ CẢNH BÁO" +
                    " tin nhắn nghi spam, không thể tự xoá/ẩn khỏi hộp thư hệ thống.",
                style = MaterialTheme.typography.bodySmall
            )

            Text("Phiên bản hiện tại: ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val update = withContext(Dispatchers.IO) {
                            UpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME)
                        }
                        if (update == null) {
                            Toast.makeText(context, "Bạn đang dùng bản mới nhất", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Có bản mới: v${update.versionName} — đang mở trang tải", Toast.LENGTH_LONG).show()
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl)))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kiểm tra cập nhật")
            }
        }
    }
}
