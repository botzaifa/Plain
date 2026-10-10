package app.plain.launcher.apps

sealed interface SlotDecision {
    data class Resolved(val packageName: String) : SlotDecision
    data object Ambiguous : SlotDecision
    data object Unresolved : SlotDecision
}

object SlotDecisions {
    /**
     * @param resolverActivityPackage when the system resolver activity is returned instead of a real app.
     */
    fun decide(
        candidates: List<String>,
        resolverActivityPackage: String?,
    ): SlotDecision {
        val distinct = candidates.distinct()
        return when {
            distinct.isEmpty() -> SlotDecision.Unresolved
            distinct.size == 1 -> {
                val pkg = distinct.single()
                if (resolverActivityPackage != null && pkg == resolverActivityPackage) {
                    SlotDecision.Ambiguous
                } else {
                    SlotDecision.Resolved(pkg)
                }
            }
            else -> SlotDecision.Ambiguous
        }
    }
}
