package app.plain.launcher

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PlainScreen {
    data object Home : PlainScreen
    data object Search : PlainScreen
    data object Gate : PlainScreen
    data object GatedList : PlainScreen
    data object Settings : PlainScreen
    data object Checklist : PlainScreen
}

class PlainViewModel : ViewModel() {
    private val _screen = MutableStateFlow<PlainScreen>(PlainScreen.Home)
    val screen: StateFlow<PlainScreen> = _screen.asStateFlow()

    private val _launchError = MutableStateFlow<String?>(null)
    val launchError: StateFlow<String?> = _launchError.asStateFlow()

    fun navigateTo(screen: PlainScreen) {
        _screen.value = screen
    }

    fun resetToHome() {
        _screen.value = PlainScreen.Home
    }

    fun showLaunchError(message: String) {
        _launchError.value = message
    }

    fun clearLaunchError() {
        _launchError.value = null
    }
}
