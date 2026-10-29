package com.nxt.callguard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nxt.callguard.data.ActivityLogEntry
import com.nxt.callguard.data.BlockSource
import com.nxt.callguard.data.BlockedNumber
import com.nxt.callguard.data.CallGuardRepository
import com.nxt.callguard.data.SpamCategory
import com.nxt.callguard.data.SpamReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CallGuardViewModel(private val repository: CallGuardRepository) : ViewModel() {

    val blockedNumbers: StateFlow<List<BlockedNumber>> =
        repository.blockedNumbers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spamReports: StateFlow<List<SpamReport>> =
        repository.spamReports.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivity: StateFlow<List<ActivityLogEntry>> =
        repository.recentActivity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _blockedToday = MutableStateFlow(0)
    val blockedToday: StateFlow<Int> = _blockedToday

    init {
        refreshBlockedToday()
    }

    private fun refreshBlockedToday() {
        viewModelScope.launch { _blockedToday.value = repository.blockedTodayCount() }
    }

    fun addToBlocklist(number: String, reason: String) {
        viewModelScope.launch {
            repository.addToBlocklist(number, reason.ifBlank { "Thêm bởi người dùng" }, BlockSource.MANUAL)
            refreshBlockedToday()
        }
    }

    fun removeFromBlocklist(entry: BlockedNumber) {
        viewModelScope.launch { repository.removeFromBlocklist(entry) }
    }

    fun reportNumber(number: String, category: SpamCategory, note: String) {
        viewModelScope.launch {
            repository.reportNumber(number, category, note)
            refreshBlockedToday()
        }
    }

    companion object {
        fun factory(repository: CallGuardRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CallGuardViewModel(repository) as T
        }
    }
}
