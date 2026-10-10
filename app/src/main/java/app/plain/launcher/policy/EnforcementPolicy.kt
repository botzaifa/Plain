package app.plain.launcher.policy

import app.plain.launcher.auth.UnlockState

enum class EnforcementAction {
    ALLOW,
    SEND_HOME,
}

object EnforcementPolicy {
    fun decide(
        foregroundPkg: String?,
        state: UnlockState,
        launchable: Set<String>,
        basic: Set<String>,
        alwaysAllowed: Set<String>,
        adminSettingsPassActive: Boolean,
        enforcementEnabled: Boolean,
        enforcementPaused: Boolean,
        allowSettingsUntilA11yOn: Boolean = false,
        settingsPackages: Set<String> = emptySet(),
        interactionPackages: Set<String> = emptySet(),
    ): EnforcementAction {
        if (!enforcementEnabled || enforcementPaused) return EnforcementAction.ALLOW
        if (foregroundPkg.isNullOrBlank()) return EnforcementAction.ALLOW
        if (adminSettingsPassActive) return EnforcementAction.ALLOW
        if (allowSettingsUntilA11yOn && settingsPackages.contains(foregroundPkg)) {
            return EnforcementAction.ALLOW
        }
        if (alwaysAllowed.contains(foregroundPkg)) return EnforcementAction.ALLOW
        if (basic.contains(foregroundPkg)) return EnforcementAction.ALLOW
        if (!launchable.contains(foregroundPkg)) return EnforcementAction.ALLOW
        if (interactionPackages.contains(foregroundPkg)) return EnforcementAction.ALLOW
        if (state == UnlockState.FULL_UNLOCKED) return EnforcementAction.ALLOW
        if (state == UnlockState.TEMP_UNLOCKED) return EnforcementAction.ALLOW
        return EnforcementAction.SEND_HOME
    }
}
