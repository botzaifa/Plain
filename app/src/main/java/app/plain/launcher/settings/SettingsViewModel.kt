package app.plain.launcher.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.plain.launcher.apps.ResolvedSlot
import app.plain.launcher.apps.Tier
import app.plain.launcher.data.AppEntryEntity
import app.plain.launcher.data.AppRepository
import app.plain.launcher.data.GateRepository
import app.plain.launcher.data.GatePassEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: AppRepository,
    private val gateRepository: GateRepository,
) : ViewModel() {
    val entries: StateFlow<List<AppEntryEntity>> =
        repository.allEntries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unresolved: StateFlow<List<ResolvedSlot>> =
        repository.unresolvedSlots()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val gateHistory: StateFlow<List<GatePassEntity>> =
        kotlinx.coroutines.flow.flow {
            emit(gateRepository.historyLastDays())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setTier(packageName: String, tier: Tier, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.setTier(packageName, tier)
            } catch (e: Exception) {
                onError(e.message ?: "failed")
            }
        }
    }

    fun moveHome(packageName: String, direction: Int) {
        viewModelScope.launch { repository.moveHome(packageName, direction) }
    }

    fun restoreDefaults(onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.restoreDefaults()
            } catch (e: Exception) {
                onError(e.message ?: "failed")
            }
        }
    }
}
