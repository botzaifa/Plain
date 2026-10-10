package app.plain.launcher.gated

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
import app.plain.launcher.databinding.ItemGatedAppBinding

class GatedListAdapter(
    private val iconLoader: IconLoader,
    private val iconSizePx: Int,
    private val onClick: (AppEntry) -> Unit,
) : RecyclerView.Adapter<GatedListAdapter.VH>(), IconLoader.Target {
    private var items: List<AppEntry> = emptyList()
    private val iconCache = mutableMapOf<String, Drawable>()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun submit(list: List<AppEntry>) {
        items = list
        mainHandler.post { notifyDataSetChanged() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemGatedAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun onIconLoaded(packageName: String, drawable: Drawable?) {
        if (drawable == null) return
        iconCache[packageName] = drawable
        val index = items.indexOfFirst { it.packageName == packageName }
        if (index < 0) return
        mainHandler.post { notifyItemChanged(index) }
    }

    inner class VH(private val binding: ItemGatedAppBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: AppEntry) {
            binding.label.text = entry.label
            binding.root.contentDescription = entry.label
            val cached = iconCache[entry.packageName]
            if (cached != null) {
                binding.icon.setImageDrawable(cached)
            } else {
                binding.icon.setImageDrawable(
                    ColorDrawable(ContextCompat.getColor(binding.root.context, R.color.plain_gray)),
                )
                iconLoader.load(entry.packageName, iconSizePx, this@GatedListAdapter)
            }
            binding.root.setOnClickListener { onClick(entry) }
        }
    }
}
