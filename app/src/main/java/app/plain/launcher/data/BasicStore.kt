package app.plain.launcher.data

import app.plain.launcher.core.KeyValueStore
import app.plain.launcher.policy.BasicRules

class BasicStore(
    private val store: KeyValueStore,
) {
    fun getOrdered(): List<String> {
        val raw = store.getString(KEY_ORDER, "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split(SEP).filter { it.isNotBlank() }
    }

    fun setOrdered(packages: List<String>) {
        store.putString(KEY_ORDER, packages.joinToString(SEP))
    }

    fun ensureWithDialer(dialer: String?, launchable: Set<String>): List<String> {
        var list = BasicRules.pruneMissing(getOrdered(), launchable)
        list = BasicRules.ensureDialerPresent(list, dialer)
        if (list != getOrdered()) setOrdered(list)
        return list
    }

    companion object {
        private const val KEY_ORDER = "basic_order"
        private const val SEP = ","
    }
}
