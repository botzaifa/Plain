package app.plain.launcher.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Telephony
import android.telecom.TelecomManager

data class ResolvedSlot(
    val slot: Slot,
    val decision: SlotDecision,
    val launchIntent: Intent? = null,
)

class SlotResolver(
    private val context: Context,
) : SlotResolverReader {
    private val packageManager: PackageManager = context.packageManager

    override fun resolve(slot: Slot, overridePackage: String?): ResolvedSlot {
        overridePackage?.let { pkg ->
            if (isInstalled(pkg)) {
                return ResolvedSlot(slot, SlotDecision.Resolved(pkg))
            }
        }
        return when (slot) {
            Slot.Phone -> resolvePhone()
            Slot.Messages -> resolveMessages()
            Slot.WhatsApp -> resolveFixed("com.whatsapp")
            Slot.Camera -> resolveActivity(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            Slot.Photos -> resolvePhotos()
            Slot.Spotify -> resolveFixed("com.spotify.music")
            Slot.Settings -> ResolvedSlot(
                slot,
                SlotDecision.Resolved("com.android.settings"),
                Intent(Settings.ACTION_SETTINGS),
            )
            Slot.Clock -> resolveClock()
            Slot.Contacts -> resolveCategory(Intent.CATEGORY_APP_CONTACTS)
            Slot.Calculator -> resolveCategory(Intent.CATEGORY_APP_CALCULATOR)
            Slot.Maps -> resolveMaps()
            Slot.Files -> resolveFiles()
            Slot.Uber -> resolveByCandidates(Slot.Uber, listOf("com.ubercab", "com.uber.app"))
            Slot.Rapido -> resolveByLabel(Slot.Rapido, "rapido")
            Slot.MIndicator -> resolveByLabel(Slot.MIndicator, "mindicator")
            Slot.Paytm -> resolveByCandidates(Slot.Paytm, listOf("net.one97.paytm", "com.paytm"))
            Slot.PhysicsWallah -> resolveByLabel(Slot.PhysicsWallah, "physicswallah")
            Slot.PlayStore -> resolveFixed("com.android.vending")
            Slot.FotMob -> resolveByLabel(Slot.FotMob, "fotmob")
            Slot.Internet -> resolveInternet()
            Slot.Duolingo -> resolveByLabel(Slot.Duolingo, "duolingo")
        }
    }

    private fun resolvePhone(): ResolvedSlot {
        val telecom = context.getSystemService(TelecomManager::class.java)
        val dialer = telecom?.defaultDialerPackage
        if (dialer != null && isInstalled(dialer)) {
            return ResolvedSlot(Slot.Phone, SlotDecision.Resolved(dialer))
        }
        val intent = Intent(Intent.ACTION_DIAL)
        return resolveIntent(slot = Slot.Phone, intent = intent)
    }

    private fun resolveMessages(): ResolvedSlot {
        val sms = Telephony.Sms.getDefaultSmsPackage(context)
        if (sms != null && isInstalled(sms)) {
            return ResolvedSlot(Slot.Messages, SlotDecision.Resolved(sms))
        }
        return ResolvedSlot(Slot.Messages, SlotDecision.Unresolved)
    }

    private fun resolveClock(): ResolvedSlot {
        val alarms = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
        val resolved = resolveIntent(Slot.Clock, alarms)
        if (resolved.decision is SlotDecision.Resolved) return resolved
        val timers = Intent(android.provider.AlarmClock.ACTION_SHOW_TIMERS)
        return resolveIntent(Slot.Clock, timers)
    }

    private fun resolvePhotos(): ResolvedSlot {
        val gallery = resolveCategory(Intent.CATEGORY_APP_GALLERY)
        if (gallery.decision is SlotDecision.Resolved) return gallery
        val view = Intent(Intent.ACTION_VIEW).setType("image/*")
        return resolveIntent(Slot.Photos, view)
    }

    private fun resolveMaps(): ResolvedSlot {
        val maps = resolveCategory(Intent.CATEGORY_APP_MAPS)
        if (maps.decision is SlotDecision.Resolved) return maps
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"))
        return resolveIntent(Slot.Maps, geo)
    }

    private fun resolveFiles(): ResolvedSlot {
        val files = resolveCategory(Intent.CATEGORY_APP_FILES)
        if (files.decision is SlotDecision.Resolved) return files
        val downloads = Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS)
        return resolveIntent(Slot.Files, downloads)
    }

    private fun resolveInternet(): ResolvedSlot {
        val browser = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_BROWSER)
        val browserResolved = resolveIntent(Slot.Internet, browser)
        if (browserResolved.decision is SlotDecision.Resolved) return browserResolved
        val https = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"))
        return resolveIntent(Slot.Internet, https)
    }

    private fun resolveCategory(category: String): ResolvedSlot {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        return resolveIntent(slotFromCategory(category), intent)
    }

    private fun slotFromCategory(category: String): Slot {
        return when (category) {
            Intent.CATEGORY_APP_CONTACTS -> Slot.Contacts
            Intent.CATEGORY_APP_CALCULATOR -> Slot.Calculator
            Intent.CATEGORY_APP_MAPS -> Slot.Maps
            Intent.CATEGORY_APP_FILES -> Slot.Files
            Intent.CATEGORY_APP_GALLERY -> Slot.Photos
            Intent.CATEGORY_APP_BROWSER -> Slot.Internet
            else -> Slot.Internet
        }
    }

    private fun resolveActivity(action: String): ResolvedSlot {
        val intent = Intent(action)
        return resolveIntent(Slot.Camera, intent)
    }

    private fun resolveFixed(packageName: String): ResolvedSlot {
        return if (isInstalled(packageName)) {
            ResolvedSlot(slotForPackage(packageName), SlotDecision.Resolved(packageName))
        } else {
            ResolvedSlot(slotForPackage(packageName), SlotDecision.Unresolved)
        }
    }

    private fun slotForPackage(packageName: String): Slot {
        return when (packageName) {
            "com.whatsapp" -> Slot.WhatsApp
            "com.spotify.music" -> Slot.Spotify
            "com.android.vending" -> Slot.PlayStore
            else -> Slot.Internet
        }
    }

    private fun resolveByCandidates(slot: Slot, candidates: List<String>): ResolvedSlot {
        val installed = candidates.filter { isInstalled(it) }
        val decision = SlotDecisions.decide(installed, resolverPackage())
        return ResolvedSlot(slot, decision)
    }

    private fun resolveByLabel(slot: Slot, needle: String): ResolvedSlot {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val matches = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.packageName }
            .distinct()
            .filter { pkg ->
                val label = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(pkg, 0),
                ).toString().lowercase()
                label.contains(needle) || pkg.lowercase().contains(needle)
            }
        val decision = SlotDecisions.decide(matches, resolverPackage())
        return ResolvedSlot(slot, decision)
    }

    private fun resolveIntent(slot: Slot, intent: Intent): ResolvedSlot {
        val matches = queryPackages(intent)
        val decision = SlotDecisions.decide(matches, resolverPackage())
        val launchIntent = if (decision is SlotDecision.Resolved) intent else null
        return ResolvedSlot(slot, decision, launchIntent)
    }

    private fun queryPackages(intent: Intent): List<String> {
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.packageName }
            .distinct()
    }

    private fun resolverPackage(): String? {
        return packageManager.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            PackageManager.MATCH_DEFAULT_ONLY,
        )?.activityInfo?.packageName
    }

    private fun isInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
