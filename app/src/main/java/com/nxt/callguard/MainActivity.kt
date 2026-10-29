package com.nxt.callguard

import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.nxt.callguard.ui.CallGuardViewModel
import com.nxt.callguard.ui.navigation.CallGuardNavHost
import com.nxt.callguard.ui.theme.CallGuardTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CallGuardViewModel by viewModels {
        CallGuardViewModel.factory((application as CallGuardApp).repository)
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* no-op, UI reads live state */ }

    private val requestCallScreeningRoleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { /* no-op */ }

    private val requiredPermissions = buildList {
        add(android.Manifest.permission.READ_PHONE_STATE)
        add(android.Manifest.permission.READ_CALL_LOG)
        add(android.Manifest.permission.READ_CONTACTS)
        add(android.Manifest.permission.RECEIVE_SMS)
        add(android.Manifest.permission.READ_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CallGuardTheme {
                CallGuardNavHost(
                    viewModel = viewModel,
                    onRequestRuntimePermissions = { requestPermissionsLauncher.launch(requiredPermissions) },
                    onRequestCallScreeningRole = { requestCallScreeningRole() }
                )
            }
        }
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(this, "Thiết bị này không hỗ trợ tính năng chặn gọi mặc định (cần Android 10+)", Toast.LENGTH_LONG).show()
            return
        }
        val roleManager = getSystemService(RoleManager::class.java)
        when {
            !roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) ->
                Toast.makeText(this, "Thiết bị/ROM này không hỗ trợ vai trò chặn gọi mặc định", Toast.LENGTH_LONG).show()
            roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ->
                Toast.makeText(this, "CallGuard đã là app chặn gọi mặc định rồi", Toast.LENGTH_LONG).show()
            else -> {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                requestCallScreeningRoleLauncher.launch(intent)
            }
        }
    }
}
