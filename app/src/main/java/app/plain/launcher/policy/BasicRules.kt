package app.plain.launcher.policy

object BasicRules {
    fun ensureDialerPresent(basicOrder: List<String>, dialerPackage: String?): List<String> {
        if (dialerPackage.isNullOrBlank()) return basicOrder
        if (basicOrder.contains(dialerPackage)) return basicOrder
        return listOf(dialerPackage) + basicOrder
    }

    fun canRemoveFromBasic(packageName: String, dialerPackage: String?): Boolean {
        if (dialerPackage.isNullOrBlank()) return true
        return packageName != dialerPackage
    }

    fun pruneMissing(basicOrder: List<String>, launchable: Set<String>): List<String> {
        return basicOrder.filter { launchable.contains(it) }
    }

    fun mergeDefaults(defaults: List<String>, existing: List<String>, launchable: Set<String>): List<String> {
        val result = mutableListOf<String>()
        for (pkg in existing) {
            if (launchable.contains(pkg) && !result.contains(pkg)) result.add(pkg)
        }
        for (pkg in defaults) {
            if (launchable.contains(pkg) && !result.contains(pkg)) result.add(pkg)
        }
        return result
    }
}
