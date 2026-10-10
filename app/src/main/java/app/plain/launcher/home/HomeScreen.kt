package app.plain.launcher.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.plain.launcher.ui.components.TextRow
import app.plain.launcher.ui.glass.GlassSurface
import app.plain.launcher.ui.theme.LocalPlainTypography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    hazeState: Any?,
    onOpenSearch: () -> Unit,
    onTapClock: () -> Unit,
    onLongPressClock: () -> Unit,
    onLaunchApp: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val type = LocalPlainTypography.current
    val time by viewModel.time.collectAsStateWithLifecycle()
    val alarm by viewModel.nextAlarm.collectAsStateWithLifecycle()
    val slots by viewModel.homeSlots.collectAsStateWithLifecycle()

    val gesturePadding = WindowInsets.systemGestures.asPaddingValues()
    val density = LocalDensity.current
    val gestureLeftPx = with(density) { gesturePadding.calculateLeftPadding(LayoutDirection.Ltr).toPx() }
    val gestureRightPx = with(density) { gesturePadding.calculateRightPadding(LayoutDirection.Ltr).toPx() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            .pointerInput(gestureLeftPx, gestureRightPx) {
                detectVerticalDragGestures { change, dragAmount ->
                    val x = change.position.x
                    if (x < gestureLeftPx || x > size.width - gestureRightPx) return@detectVerticalDragGestures
                    if (dragAmount < -12f) onOpenSearch()
                }
            },
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        GlassSurface(
            hazeState = hazeState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .combinedClickable(
                        onClick = onTapClock,
                        onLongClick = onLongPressClock,
                    ),
            ) {
                BasicText(text = time.timeText, style = type.clock)
                BasicText(text = time.dateText, style = type.date, modifier = Modifier.padding(top = 4.dp))
                alarm?.let {
                    BasicText(text = it, style = type.alarmLine, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            slots.forEach { slot ->
                TextRow(
                    text = slot.label,
                    style = type.homeApp,
                    onClick = { slot.packageName?.let(onLaunchApp) },
                )
            }
            TextRow(text = "search", style = type.hint, onClick = onOpenSearch)
        }
    }
}
