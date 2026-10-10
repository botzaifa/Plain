package app.plain.launcher.gate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.plain.launcher.ui.components.TextRow
import app.plain.launcher.ui.theme.LocalPlainTypography
import kotlinx.coroutines.delay

@Composable
fun GateScreen(
    gateSession: GateSessionController,
    waitSeconds: Int,
    purpose: GatePurpose,
    onFinished: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val type = LocalPlainTypography.current
    val state by gateSession.state.collectAsStateWithLifecycle()
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                gateSession.cancel()
                onCancel()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        view.keepScreenOn = true
        gateSession.beginWait(purpose, waitSeconds, android.os.SystemClock.elapsedRealtime())
    }

    DisposableEffect(Unit) {
        onDispose { view.keepScreenOn = false }
    }

    var remaining by remember { mutableIntStateOf(waitSeconds) }

    LaunchedEffect(state, waitSeconds) {
        val waiting = state as? GateSessionState.Waiting ?: return@LaunchedEffect
        while (remaining > 0) {
            delay(1000)
            remaining -= 1
        }
        gateSession.onCountdownFinished()
        onFinished()
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BasicText(text = "All apps", style = type.title)
        BasicText(
            text = remaining.toString(),
            style = type.clock,
            modifier = Modifier.padding(top = 48.dp),
        )
        TextRow(
            text = "Cancel",
            style = type.hint,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            onClick = {
                gateSession.cancel()
                onCancel()
            },
        )
    }
}
