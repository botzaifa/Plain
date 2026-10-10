package app.plain.launcher.home

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.plain.launcher.R
import app.plain.launcher.apps.AppEntry
import app.plain.launcher.apps.IconLoader
import app.plain.launcher.databinding.ItemHomeTileBinding

sealed class HomeTile {
    data class App(val entry: AppEntry) : HomeTile()
    data object More : HomeTile()
}

class HomeGridAdapter(
    private val iconLoader: IconLoader,
    private val iconSizePx: Int,
    private val onAppClick: (AppEntry) -> Unit,
    private val onMoreClick: () -> Unit,
) : RecyclerView.Adapter<HomeGridAdapter.VH>(), IconLoader.Target {
    private var items: List<HomeTile> = emptyList()
    private val iconCache = mutableMapOf<String, Drawable>()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun submit(apps: List<AppEntry>) {
        items = apps.map { HomeTile.App(it) } + HomeTile.More
        mainHandler.post { notifyDataSetChanged() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHomeTileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (val tile = items[position]) {
            is HomeTile.App -> holder.bindApp(tile.entry)
            HomeTile.More -> holder.bindMore()
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onIconLoaded(packageName: String, drawable: Drawable?) {
        if (drawable == null) return
        iconCache[packageName] = drawable
        val index = items.indexOfFirst { it is HomeTile.App && it.entry.packageName == packageName }
        if (index < 0) return
        mainHandler.post { notifyItemChanged(index) }
    }

    inner class VH(private val binding: ItemHomeTileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindApp(entry: AppEntry) {
            binding.label.text = entry.label
            binding.root.contentDescription = entry.label
            val cached = iconCache[entry.packageName]
            if (cached != null) {
                binding.icon.setImageDrawable(cached)
            } else {
                binding.icon.setImageDrawable(
                    ColorDrawable(ContextCompat.getColor(binding.root.context, R.color.plain_gray)),
                )
                iconLoader.load(entry.packageName, iconSizePx, this@HomeGridAdapter)
            }
            binding.root.setOnClickListener { onAppClick(entry) }
        }

        fun bindMore() {
            binding.label.setText(R.string.more)
            binding.root.contentDescription = binding.root.context.getString(R.string.more)
            binding.icon.setImageResource(android.R.drawable.ic_menu_more)
            binding.root.setOnClickListener { onMoreClick() }
        }
    }
}
