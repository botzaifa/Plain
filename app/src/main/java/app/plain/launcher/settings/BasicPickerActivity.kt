package app.plain.launcher.settings

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.min
import app.plain.launcher.PlainApp
import app.plain.launcher.apps.AppEntry
import app.plain.launcher.core.EdgeInsets
import app.plain.launcher.databinding.ActivityBasicPickerBinding
import app.plain.launcher.databinding.ItemBasicPickerBinding

class BasicPickerActivity : Activity() {
    private lateinit var binding: ActivityBasicPickerBinding
    private val app get() = PlainApp.instance
    private lateinit var adapter: BasicPickerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBasicPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EdgeInsets.applySystemBars(binding.basicPickerRoot)
        val dialer = app.defaults.dialer()
        adapter = BasicPickerAdapter(dialer)
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.itemAnimator = BasicPickerAdapter.createItemAnimator()
        binding.list.adapter = adapter
        adapter.setData(buildRows())
        ItemTouchHelper(BasicMoveCallback(adapter)).attachToRecyclerView(binding.list)
        binding.btnSave.setOnClickListener {
            app.basicStore.setOrdered(adapter.orderedSelected())
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun buildRows(): List<BasicPickerAdapter.Row> {
        val launchable = app.catalog.getLaunchable()
        val selected = app.basicStore.ensureWithDialer(app.defaults.dialer(), app.catalog.launchablePackages())
        val selectedSet = selected.toSet()
        val ordered = selected.mapNotNull { pkg -> launchable.find { it.packageName == pkg } }
        val rest = launchable.filter { !selectedSet.contains(it.packageName) }.sortedBy { it.sortKey }
        return ordered.map { BasicPickerAdapter.Row(it, true) } +
            rest.map { BasicPickerAdapter.Row(it, false) }
    }
}

/** Shared by settings and setup wizard. */
class BasicPickerAdapter(
    private val dialerPackage: String?,
) : RecyclerView.Adapter<BasicPickerAdapter.VH>() {
    data class Row(val entry: AppEntry, var checked: Boolean)

    private val rows = mutableListOf<Row>()
    private var recyclerView: RecyclerView? = null

    init {
        setHasStableIds(true)
    }

    fun setData(data: List<Row>) {
        rows.clear()
        rows.addAll(data)
        notifyDataSetChanged()
    }

    fun orderedSelected(): List<String> = rows.filter { it.checked }.map { it.entry.packageName }

    fun moveChecked(from: Int, to: Int) {
        if (from !in rows.indices || to !in rows.indices) return
        if (!rows[from].checked || !rows[to].checked) return
        val item = rows.removeAt(from)
        rows.add(to, item)
        notifyItemMoved(from, to)
    }

    fun checkedSpan(): IntRange {
        val last = rows.indexOfLast { it.checked }
        return if (last < 0) 0..-1 else 0..last
    }

    override fun getItemId(position: Int): Long = rows[position].entry.packageName.hashCode().toLong()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        if (this.recyclerView === recyclerView) this.recyclerView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBasicPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(rows[position])
    }

    override fun getItemCount(): Int = rows.size

    fun applyCheckedChange(row: Row, isChecked: Boolean) {
        val pos = rows.indexOf(row)
        if (pos < 0 || row.checked == isChecked) return
        row.checked = isChecked
        rows.removeAt(pos)
        val insertAt =
            if (isChecked) {
                rows.indexOfLast { it.checked } + 1
            } else {
                rows.indexOfFirst { !it.checked }.let { if (it < 0) rows.size else it }
            }
        rows.add(insertAt, row)
        if (insertAt == pos) {
            notifyItemChanged(insertAt)
            if (isChecked) scrollToReveal(insertAt)
            return
        }
        syncRowAppearance(row, pos)
        notifyItemMoved(pos, insertAt)
        val rangeStart = min(pos, insertAt)
        val rangeCount = abs(pos - insertAt) + 1
        runAfterMoveAnimations {
            notifyItemRangeChanged(rangeStart, rangeCount)
            if (isChecked) scrollToReveal(insertAt)
        }
    }

    private fun syncRowAppearance(row: Row, position: Int) {
        val holder = recyclerView?.findViewHolderForAdapterPosition(position) as? VH ?: return
        holder.syncAppearance(row)
    }

    private fun runAfterMoveAnimations(action: () -> Unit) {
        val rv = recyclerView ?: run {
            action()
            return
        }
        val animator = rv.itemAnimator
        if (animator == null || !animator.isRunning) {
            rv.post(action)
        } else {
            animator.isRunning { rv.post(action) }
        }
    }

    private fun scrollToReveal(insertAt: Int) {
        val rv = recyclerView ?: return
        rv.post {
            val lm = rv.layoutManager as? LinearLayoutManager ?: return@post
            val scroller =
                object : LinearSmoothScroller(rv.context) {
                    override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                }
            scroller.targetPosition = insertAt
            lm.startSmoothScroll(scroller)
        }
    }

    inner class VH(private val binding: ItemBasicPickerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun syncAppearance(row: Row) {
            val locked = row.entry.packageName == dialerPackage
            binding.check.setOnCheckedChangeListener(null)
            binding.check.isChecked = row.checked || locked
            val reorderVisibility = if (row.checked || locked) View.VISIBLE else View.GONE
            binding.btnUp.visibility = reorderVisibility
            binding.btnDown.visibility = reorderVisibility
        }

        fun bind(row: Row) {
            val locked = row.entry.packageName == dialerPackage
            binding.label.text = if (locked) "${row.entry.label} *" else row.entry.label
            binding.check.isEnabled = !locked
            syncAppearance(row)
            binding.toggleArea.isClickable = !locked
            binding.toggleArea.setOnClickListener {
                if (locked) return@setOnClickListener
                applyCheckedChange(row, !row.checked)
            }
            binding.btnUp.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val prev = (pos - 1 downTo 0).firstOrNull { rows[it].checked } ?: return@setOnClickListener
                moveChecked(pos, prev)
            }
            binding.btnDown.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val next = (pos + 1 until rows.size).firstOrNull { rows[it].checked } ?: return@setOnClickListener
                moveChecked(pos, next)
            }
        }
    }

    companion object {
        fun createItemAnimator(): RecyclerView.ItemAnimator =
            DefaultItemAnimator().apply {
                supportsChangeAnimations = false
                addDuration = 0L
                removeDuration = 0L
                changeDuration = 0L
                moveDuration = 380L
            }
    }
}

internal class BasicMoveCallback(
    private val adapter: BasicPickerAdapter,
) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        val from = viewHolder.bindingAdapterPosition
        val to = target.bindingAdapterPosition
        if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
        adapter.moveChecked(from, to)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

    override fun canDropOver(
        recyclerView: RecyclerView,
        current: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        val to = target.bindingAdapterPosition
        return to in adapter.checkedSpan()
    }

    override fun isLongPressDragEnabled(): Boolean = false
}
