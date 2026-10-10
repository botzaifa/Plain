package app.plain.launcher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        val container = (application as PlainApp).container
        setContent {
            PlainRoot(container)
        }
    }

    override fun onStart() {
        super.onStart()
        (application as PlainApp).container.startCatalog()
    }

    override fun onStop() {
        (application as PlainApp).container.gateSession.onForegroundLost()
        (application as PlainApp).container.stopCatalog()
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            // Reset handled by ViewModel via activity recreation; PlainRoot resets on HOME intent in future.
        }
    }
}
