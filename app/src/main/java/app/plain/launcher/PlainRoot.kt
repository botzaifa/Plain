package app.plain.launcher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.plain.launcher.apps.LaunchResult
import app.plain.launcher.gate.GatePurpose
import app.plain.launcher.gate.GateScreen
import app.plain.launcher.gate.GatedListScreen
import app.plain.launcher.home.HomeScreen
import app.plain.launcher.home.HomeViewModel
import app.plain.launcher.search.SearchSheet
import app.plain.launcher.search.SearchViewModel
import app.plain.launcher.settings.SettingsScreen
import app.plain.launcher.settings.SettingsViewModel
import app.plain.launcher.settings.SetupChecklist
import app.plain.launcher.ui.glass.Backdrop
import app.plain.launcher.ui.theme.PlainTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment

@Composable
fun PlainRoot(container: AppContainer) {
    PlainTheme {
        val plainVm: PlainViewModel = viewModel()
        val screen by plainVm.screen.collectAsStateWithLifecycle()
        val launchError by plainVm.launchError.collectAsStateWithLifecycle()

        val homeVm = remember {
            HomeViewModel(container.timeSource, container.nextAlarmSource, container.appRepository)
        }
        val searchVm = remember { SearchViewModel(container.appRepository) }
        val settingsVm = remember {
            SettingsViewModel(container.appRepository, container.gateRepository)
        }

        val hazeState = remember { Any() }

        LaunchedEffect(Unit) {
            container.startCatalog()
            container.appRepository.ensureFirstRun()
        }

        BackHandler(enabled = screen == PlainScreen.Home) { }

        Box(modifier = Modifier.fillMaxSize()) {
            Backdrop()
            when (screen) {
                PlainScreen.Home -> {
                    HomeScreen(
                        viewModel = homeVm,
                        hazeState = hazeState,
                        onOpenSearch = { plainVm.navigateTo(PlainScreen.Search) },
                        onTapClock = { container.launchClock() },
                        onLongPressClock = { plainVm.navigateTo(PlainScreen.Settings) },
                        onLaunchApp = { pkg -> container.launchPackage(pkg, plainVm) },
                    )
                }
                PlainScreen.Search -> {
                    BackHandler { plainVm.navigateTo(PlainScreen.Home) }
                    SearchSheet(
                        viewModel = searchVm,
                        hazeState = hazeState,
                        onLaunchApp = { pkg -> container.launchPackage(pkg, plainVm) },
                        onAllApps = {
                            container.applicationScope.launch {
                                val passes = container.gateRepository.passesToday()
                                val wait = app.plain.launcher.gate.GatePolicy.waitSeconds(passes)
                                container.gateSession.beginWait(
                                    GatePurpose.Launch,
                                    wait,
                                    android.os.SystemClock.elapsedRealtime(),
                                )
                                plainVm.navigateTo(PlainScreen.Gate)
                            }
                        },
                    )
                }
                PlainScreen.Gate -> {
                    BackHandler {
                        container.gateSession.cancel()
                        plainVm.navigateTo(PlainScreen.Search)
                    }
                    GateScreen(
                        gateSession = container.gateSession,
                        waitSeconds = app.plain.launcher.gate.GatePolicy.waitSeconds(0),
                        purpose = GatePurpose.Launch,
                        onFinished = { plainVm.navigateTo(PlainScreen.GatedList) },
                        onCancel = { plainVm.navigateTo(PlainScreen.Search) },
                    )
                }
                PlainScreen.GatedList -> {
                    BackHandler { plainVm.resetToHome() }
                    GatedListScreen(
                        repository = container.appRepository,
                        onLaunch = { pkg ->
                            container.launchGated(pkg, plainVm)
                        },
                    )
                }
                PlainScreen.Settings -> {
                    BackHandler { plainVm.resetToHome() }
                    SettingsScreen(
                        viewModel = settingsVm,
                        repository = container.appRepository,
                        hazeState = hazeState,
                        onOpenChecklist = { plainVm.navigateTo(PlainScreen.Checklist) },
                        onError = { plainVm.showLaunchError(it) },
                    )
                }
                PlainScreen.Checklist -> {
                    BackHandler { plainVm.navigateTo(PlainScreen.Settings) }
                    SetupChecklist(
                        packageNames = container.appCatalog.apps.value.map { it.packageName },
                        keepOn = setOf("com.android.server.telecom", "com.whatsapp"),
                    )
                }
            }
            launchError?.let {
                androidx.compose.foundation.text.BasicText(
                    text = it,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
                )
            }
        }
    }
}

private fun AppContainer.launchPackage(packageName: String, plainVm: PlainViewModel) {
    applicationScope.launch {
        val activity = appRepository.launchActivityClass(packageName, null) ?: return@launch
        when (appLauncher.launch(packageName, activity)) {
            is LaunchResult.Failed -> plainVm.showLaunchError("couldn't open")
            LaunchResult.Accepted -> Unit
        }
    }
}

private fun AppContainer.launchGated(packageName: String, plainVm: PlainViewModel) {
    applicationScope.launch {
        if (!gateSession.consumeLaunchAuthorization()) return@launch
        val activity = appRepository.launchActivityClass(packageName, null) ?: return@launch
        when (val result = appLauncher.launch(packageName, activity)) {
            LaunchResult.Accepted -> gateRepository.recordSuccessfulPass()
            is LaunchResult.Failed -> {
                gateSession.beginWait(GatePurpose.Launch, 0, android.os.SystemClock.elapsedRealtime())
                plainVm.showLaunchError("couldn't open")
            }
        }
    }
}

private fun AppContainer.launchClock() {
    val intent = android.content.Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
    appLauncher.launchIntent(intent)
}
