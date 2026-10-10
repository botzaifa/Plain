package app.plain.launcher.apps

enum class Tier {
    HOME,
    TOOLS,
    GATED,
}

enum class Slot(
    val defaultTier: Tier,
    val protected: Boolean,
) {
    Phone(Tier.HOME, true),
    Messages(Tier.HOME, true),
    WhatsApp(Tier.HOME, false),
    Camera(Tier.HOME, false),
    Photos(Tier.HOME, false),
    Spotify(Tier.HOME, false),
    Settings(Tier.HOME, true),
    Clock(Tier.TOOLS, true),
    Contacts(Tier.TOOLS, false),
    Calculator(Tier.TOOLS, false),
    Maps(Tier.TOOLS, false),
    Files(Tier.TOOLS, false),
    Uber(Tier.TOOLS, false),
    Rapido(Tier.TOOLS, false),
    MIndicator(Tier.TOOLS, false),
    Paytm(Tier.TOOLS, false),
    PhysicsWallah(Tier.TOOLS, false),
    PlayStore(Tier.GATED, false),
    FotMob(Tier.GATED, false),
    Internet(Tier.GATED, false),
    Duolingo(Tier.GATED, false),
    ;

    val storageKey: String get() = name

    companion object {
        val homeOrder: List<Slot> = listOf(
            Phone,
            Messages,
            WhatsApp,
            Camera,
            Photos,
            Spotify,
            Settings,
        )

        val toolsSlots: List<Slot> = entries.filter { it.defaultTier == Tier.TOOLS }

        val gatedPinnedBottom: List<Slot> = listOf(Internet, Duolingo)
    }
}
