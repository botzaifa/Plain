# API notes

## compileSdk / targetSdk

- **37.0** (stable platform installed at `/home/huzi/Android/Sdk/platforms/android-37.0`)

## minSdk 24

- AGP 9.1.0 + lint `NewApi` / `InlinedApi` fatal
- PBKDF2-HMAC-SHA256: hand-rolled (`auth/Pbkdf2.kt`); JVM unit test uses RFC 7914 vector
- Home role: `RoleManager` API 29+; API 24–28 uses `resolveActivity(HOME)` + settings fallbacks
- Predictive back: `OnBackInvokedCallback` API 33+ in `HomeActivity`
- `java.util.Base64` avoided (API 26+); hashes stored as hex

## android:isAccessibilityTool

- **Manifest attribute** added in **API 31** (Android 12). Declared in `res/xml/accessibility_service_config.xml` as `false` because Plain is not an assistive technology product.
- References: [AccessibilityServiceInfo](https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo), `R.attr.isAccessibilityTool` (API 31). On API 24–30 the attribute is ignored at parse time.
- Runtime: `AccessibilityServiceInfo.isAccessibilityTool()` API 31+.

## BOOT_COUNT

- `Settings.Global.BOOT_COUNT` from API 24; fallback if missing: `elapsedNow < tempStart` ends TEMP.

## BuiltInAllowlist

| Package | Device | API | Evidence |
|---------|--------|-----|----------|
| _(empty)_ | | | Add rows when a launchable system picker is bounced during M5 manual tests |

## ENABLED_INPUT_METHODS (targetSdk 34+)

- Reading `Settings.Secure.ENABLED_INPUT_METHODS` throws **SecurityException** when the app targets SDK 34+. Plain uses `InputMethodManager.getEnabledInputMethodList()` on API 34+ device only; older devices skip IME allowlist (keyboards may still work as non-launchable UI).

## LauncherApps empty list (Pixel / API 34+)

- Until Plain holds **HOME**, `LauncherApps.getActivityList(null, …)` can return **no apps** on newer builds.
- **Fallback:** `PackageManager.queryIntentActivities(MAIN/LAUNCHER)` (see `AppCatalog.kt`). Icons use app icons when `LauncherActivityInfo` is unavailable.

## Accessibility + Settings on Pixel

- After setup, **Settings** is gated. Enforcement allows known Settings/permission packages while the accessibility service is **not running**, so you can enable Plain gate (and on API 33+ use **Allow restricted settings** on App info).
- Tap the yellow **accessibility off** banner on Home to open Settings with a short admin pass.

## Restricted settings (API 33+)

- Sideloaded apps: enable accessibility only after **Allow restricted settings** on App info (wording varies by OEM).

## Debug TEMP override

- `TempDuration.kt` reads override only when `BuildConfig.DEBUG`. Release must contain no override strings (verify with release APK string search / full 10 min TEMP).
