package app.plain.launcher.apps

interface PackagePresenceChecker {
    var reconciliationEnabled: Boolean
    fun isAbsent(packageName: String): Boolean
}
