package app.plain.launcher.core

/** Allowed in foreground while locked so Basic apps can complete system flows (share, pickers). */
object InteractionPackages {
    val known: Set<String> =
        setOf(
            "com.android.intentresolver",
            "com.android.systemui",
            "com.android.providers.media",
            "com.google.android.providers.media.module",
        )
}
