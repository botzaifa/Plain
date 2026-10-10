package app.plain.launcher.apps

interface SlotResolverReader {
    fun resolve(slot: Slot, overridePackage: String?): ResolvedSlot
}
