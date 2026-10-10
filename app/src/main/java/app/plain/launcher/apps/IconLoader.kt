package app.plain.launcher.apps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class IconLoader(
    context: Context,
    private val catalog: AppCatalog,
    private val executor: Executor = Executors.newSingleThreadExecutor(),
) {
    interface Target {
        fun onIconLoaded(packageName: String, drawable: Drawable?)
    }

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cache: LruCache<String, Drawable>

    init {
        val memClass = (appContext.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager)
            .memoryClass
        val mb = (memClass / 16).coerceAtMost(4).coerceAtLeast(1)
        cache = object : LruCache<String, Drawable>(mb * 1024 * 1024) {
            override fun sizeOf(key: String, value: Drawable): Int {
                val bmp = (value as? BitmapDrawable)?.bitmap ?: return 1
                return bmp.byteCount
            }
        }
    }

    fun load(packageName: String, sizePx: Int, target: Target) {
        cache.get(packageName)?.let {
            target.onIconLoaded(packageName, it)
            return
        }
        executor.execute {
            val entry = catalog.findEntry(packageName)
            val source =
                entry?.activityInfo?.getBadgedIcon(appContext.resources.displayMetrics.densityDpi)
                    ?: runCatching {
                        appContext.packageManager.getApplicationIcon(packageName)
                    }.getOrNull()
            val drawable = source?.let { downsample(it, sizePx) }
            if (drawable != null) {
                cache.put(packageName, drawable)
            }
            mainHandler.post { target.onIconLoaded(packageName, drawable) }
        }
    }

    fun trimMemory() {
        cache.evictAll()
    }

    private fun downsample(source: Drawable, sizePx: Int): Drawable {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        source.setBounds(0, 0, sizePx, sizePx)
        source.draw(canvas)
        return BitmapDrawable(appContext.resources, bmp)
    }
}
