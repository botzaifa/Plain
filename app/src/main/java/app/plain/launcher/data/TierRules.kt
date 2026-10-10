package app.plain.launcher.data

import app.plain.launcher.apps.Slot
import app.plain.launcher.apps.Tier

object TierRules {
    fun isProtectedPackage(slotPackageMap: Map<Slot, String?>, packageName: String): Boolean {
        return Slot.entries.any { slot ->
            slot.protected && slotPackageMap[slot] == packageName
        }
    }

    fun canAssignTier(
        packageName: String,
        newTier: Tier,
        slotPackageMap: Map<Slot, String?>,
    ): Boolean {
        if (newTier != Tier.GATED) return true
        return !isProtectedPackage(slotPackageMap, packageName)
    }

    fun sortGatedLabels(
        items: List<Pair<String, String>>,
        internetPackage: String?,
        duolingoPackage: String?,
    ): List<Pair<String, String>> {
        val pinned = setOfNotNull(internetPackage, duolingoPackage)
        val regular = items
            .filter { it.first !in pinned }
            .sortedBy { it.second.lowercase() }
        val internet = internetPackage?.let { pkg ->
            items.find { it.first == pkg }
        }
        val duolingo = duolingoPackage?.let { pkg ->
            items.find { it.first == pkg }
        }
        return buildList {
            addAll(regular)
            internet?.let { add(it) }
            duolingo?.let { add(it) }
        }
    }

    fun requiresApplyAuthorization(oldTier: Tier, newTier: Tier): Boolean {
        return oldTier == Tier.GATED && newTier != Tier.GATED
    }
}
