package app.plain.launcher.policy

/**
 * Launchable system pickers added only after manual evidence of a bounce.
 * Start empty; extend with package names documented in docs/api-notes.md.
 */
object BuiltInAllowlist {
    val packages: Set<String> = emptySet()
}
