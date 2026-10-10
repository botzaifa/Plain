package app.plain.launcher.system

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.app.WallpaperManager
import android.widget.Toast
import app.plain.launcher.R

object WallpaperHelper {
    fun setSolidBlack(context: Context): Boolean {
        return runCatching {
            val wallpaper = WallpaperManager.getInstance(context)
            val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.RGB_565)
            bitmap.eraseColor(Color.BLACK)
            wallpaper.setBitmap(bitmap)
            Toast.makeText(context, R.string.black_wallpaper_applied, Toast.LENGTH_SHORT).show()
            true
        }.getOrElse {
            Toast.makeText(context, R.string.black_wallpaper_failed, Toast.LENGTH_SHORT).show()
            false
        }
    }
}
