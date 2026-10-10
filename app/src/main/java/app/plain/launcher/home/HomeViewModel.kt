package app.plain.launcher.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.plain.launcher.data.AppRepository
import app.plain.launcher.data.HomeSlotDisplay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    timeSource: TimeSource,
    nextAlarmSource: NextAlarmSource,
    repository: AppRepository,
) : ViewModel() {
    val time = timeSource.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimeDisplay("--:--", ""))

    val nextAlarm = nextAlarmSource.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val homeSlots: StateFlow<List<HomeSlotDisplay>> =
        repository.homeSlots()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
